package org.voxelware.coretuff.Features.Teleportation;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.voxelware.coretuff.CoreTuff;

import org.voxelware.coretuff.Utility.CoolDown;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TpManager {

	private static final long TELEPORT_DELAY_SECONDS = 5L;

	private final Map<UUID, TpRequest> requests = new ConcurrentHashMap<>();
	private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();
	private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
	private final CoreTuff plugin;
	private FileConfiguration config;
	private final IgnoreService ignoreService;
	private final DelayedTeleporter delayedTeleporter;
	private long requestTimeoutMillis;
	private List<String> tpAskReceivedMessage;
	private List<String> tpHereReceivedMessage;

	public TpManager(CoreTuff plugin, IgnoreService ignoreService, DelayedTeleporter delayedTeleporter) {
		this.plugin = plugin;
		this.ignoreService = ignoreService;
		this.delayedTeleporter = delayedTeleporter;
		reload();
	}

	public record TpRequest(UUID sender, boolean here, long createdAt) {}

	public void sendRequest(Player sender, Player target, boolean here) {
		if (sender.getUniqueId().equals(target.getUniqueId())) {
			sender.sendMessage(plugin.format(sender, config.getString("Teleportation.tpSelf"), null));
			return;
		}

		if (ignoreService.isIgnoring(target.getUniqueId(), sender.getUniqueId())) {
			sender.sendMessage(plugin.format(sender, config.getString("Teleportation.playerIgnore"), null));
			return;
		}

		requests.put(
				target.getUniqueId(),
				new TpRequest(sender.getUniqueId(), here, System.currentTimeMillis())
		);

		List<String> receivedMessage = here ? tpHereReceivedMessage : tpAskReceivedMessage;
		sender.sendMessage(plugin.format(
				target,
				config.getStringList("Teleportation.tpsenderMessage").get(0),
				Map.of("player", target.getName())
		));

		if (receivedMessage.size() >= 3) {
			target.sendMessage(plugin.format(
					sender,
					receivedMessage.get(0),
					Map.of("player", sender.getName())
			));
			target.sendMessage(plugin.format(sender, receivedMessage.get(1), null));
			target.sendMessage(plugin.format(
					sender,
					receivedMessage.get(2),
					Map.of("time", String.valueOf(config.getInt("Teleportation.tpExpire")))
			));
		}
	}

	public void acceptRequest(Player target) {
		TpRequest request = requests.get(target.getUniqueId());
		if (request == null) {
			target.sendMessage(plugin.format(target, config.getString("Teleportation.tpNoPendingReq"), null));
			return;
		}

		if (isExpired(request)) {
			requests.remove(target.getUniqueId());
			target.sendMessage(plugin.format(target, config.getString("Teleportation.tpReqExpired"), null));
			return;
		}

		requests.remove(target.getUniqueId());
		Player sender = target.getServer().getPlayer(request.sender());
		if (sender == null || !sender.isOnline()) {
			target.sendMessage(plugin.format(
					target,
					plugin.getUtilityConfig().get().getString("playerOffline"),
					null
			));
			return;
		}

		if (request.here()) {
			teleport(target, sender.getLocation(), TELEPORT_DELAY_SECONDS);
		} else {
			teleport(sender, target.getLocation(), TELEPORT_DELAY_SECONDS);
		}
	}

	public void denyRequest(Player target) {
		TpRequest request = requests.get(target.getUniqueId());
		if (request == null) {
			target.sendMessage(plugin.format(target, config.getString("Teleportation.tpNoReqDeny"), null));
			return;
		}

		if (isExpired(request)) {
			requests.remove(target.getUniqueId());
			target.sendMessage(plugin.format(target, config.getString("Teleportation.tpReqExpired"), null));
			return;
		}

		requests.remove(target.getUniqueId());
		Player sender = target.getServer().getPlayer(request.sender());
		if (sender != null) {
			sender.sendMessage(plugin.format(target, config.getString("Teleportation.tpReqDenied"), null));
		}

		target.sendMessage(plugin.format(target, config.getString("Teleportation.tpDenied"), null));
	}

	public boolean isOnCooldown(Player player, String key) {
		long cooldownMillis = cooldowns.getOrDefault(key, 0L);
		return CoolDown.isOnCooldown(key, player, cooldownMillis);
	}

	public void setCooldown(Player player, String key) {
		CoolDown.setCooldown(key, player);
	}

	public void setLastLocation(Player player) {
		lastLocations.put(player.getUniqueId(), player.getLocation().clone());
	}

	public CompletableFuture<Boolean> teleport(Player player, Location location, long delaySeconds) {
		setLastLocation(player);
		return delayedTeleporter.teleport(player, location, delaySeconds);
	}

	public CompletableFuture<Boolean> teleportBack(Player player) {
		Location location = lastLocations.get(player.getUniqueId());
		if (location == null) {
			player.sendMessage(plugin.format(player, config.getString("Teleportation.backNoPrevLocation"), null));
			return CompletableFuture.completedFuture(false);
		}

		return teleport(player, location, TELEPORT_DELAY_SECONDS);
	}

	private boolean isExpired(TpRequest request) {
		return (System.currentTimeMillis() - request.createdAt()) > requestTimeoutMillis;
	}

	public void reload() {
		config = plugin.getTeleportConfig().get();
		requestTimeoutMillis = config.getLong("Teleportation.tpExpire") * 1000L;
		tpAskReceivedMessage = config.getStringList("Teleportation.tpasktargetMessage");
		tpHereReceivedMessage = config.getStringList("Teleportation.tpheretargetMessage");

		cooldowns.clear();
		cooldowns.put("back", config.getLong("Teleportation.backCooldown") * 1000L);
		cooldowns.put("rtp", config.getLong("Teleportation.rtpCooldown") * 1000L);
		cooldowns.put("tpa", config.getLong("Teleportation.tpCooldown") * 1000L);
	}
}
