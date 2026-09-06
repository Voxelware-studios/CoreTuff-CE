package org.voxelware.coretuff.Features.Teleportation;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Teleportation.Handler.TeleportHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DelayedTeleporter {

	private final CoreTuff plugin;
	private final TeleportHandler teleportHandler;
	private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();
	private final Map<UUID, Location> startLocations = new ConcurrentHashMap<>();

	public DelayedTeleporter(CoreTuff plugin, TeleportHandler teleportHandler) {
		this.plugin = plugin;
		this.teleportHandler = teleportHandler;
	}
	public CompletableFuture<Boolean> teleport(Player player, Location to, long seconds) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		UUID uuid = player.getUniqueId();
		FileConfiguration config = plugin.getTeleportConfig().get();
		cancel(player);

		AtomicLong timeLeft = new AtomicLong(seconds);
		Location start = player.getLocation().clone();
		startLocations.put(uuid, start);

		player.sendMessage(plugin.format(
				player,
				config.getString("Teleportation.tpCountMove"),
				Map.of("time", String.valueOf(seconds))
		));

		ScheduledTask task = player.getScheduler().runAtFixedRate(plugin, scheduledTask -> {
			if (!player.isOnline()) {
				cleanup(uuid);
				future.complete(false);
				scheduledTask.cancel();
				return;
			}

			if (hasMoved(player, start)) {
				player.sendMessage(plugin.format(player, config.getString("Teleportation.tpCancMoved"), null));
				cleanup(uuid);
				future.complete(false);
				scheduledTask.cancel();
				return;
			}

			if (timeLeft.get() <= 0) {
				scheduledTask.cancel();
				cleanup(uuid);
				teleportHandler.teleport(player, to).whenComplete((success, throwable) -> {
					if (throwable != null) {
						future.complete(false);
						return;
					}

					future.complete(Boolean.TRUE.equals(success));
				});
				return;
			}

			player.sendMessage(plugin.format(
					player,
					config.getString("Teleportation.tpCountdown"),
					Map.of("time", String.valueOf(timeLeft.get()))
			));
			timeLeft.decrementAndGet();
		}, null, 1L, 20L);

		tasks.put(uuid, task);
		return future;
	}

	public void cancel(Player player) {
		UUID uuid = player.getUniqueId();

		ScheduledTask task = tasks.remove(uuid);
		if (task != null) {
			task.cancel();
		}

		startLocations.remove(uuid);
	}

	private void cleanup(UUID uuid) {
		tasks.remove(uuid);
		startLocations.remove(uuid);
	}

	private boolean hasMoved(Player player, Location start) {
		Location current = player.getLocation();
		return current.getBlockX() != start.getBlockX()
				|| current.getBlockY() != start.getBlockY()
				|| current.getBlockZ() != start.getBlockZ();
	}
	public boolean isTeleporting(Player player) {
		return tasks.containsKey(player.getUniqueId());
	}
}
