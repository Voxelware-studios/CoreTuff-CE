package org.voxelware.coretuff.Features.Teleportation;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.voxelware.coretuff.CoreTuff;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IgnoreService {
	
	private final IgnoreRepository ignoreRepo;
	private final Map<UUID, Set<UUID>> ignoreCache = new ConcurrentHashMap<>();
	private final CoreTuff plugin;
	private FileConfiguration config;
	
	public IgnoreService(IgnoreRepository ignoreRepo, CoreTuff plugin) {
		this.ignoreRepo = ignoreRepo;
		this.plugin = plugin;
		this.config = plugin.getTeleportConfig().get();
	}
	public void load() {
		ignoreCache.clear();
		ignoreCache.putAll(ignoreRepo.loadAll());
	}
	public void ignore(Player player, Player target) {
		UUID playerId = player.getUniqueId();
		UUID targetId = target.getUniqueId();
		
		if (playerId.equals(targetId)) {
			player.sendMessage(plugin.format(null, config.getString("Teleportation.ignoreYourself"), null));
			return;
		}
		
		ignoreCache.putIfAbsent(playerId, new HashSet<>());
		Set<UUID> list = ignoreCache.get(playerId);
		
		if (list.contains(targetId)) {
			player.sendMessage(plugin.format(null, config.getString("Teleportation.alreadlyIgnoring"),null));
			return;
		}
		
		list.add(targetId);
		player.sendMessage(plugin.format(target, config.getString("Teleportation.ignoreAdd"),
				Map.of(
						"player",target.getName())));
		ignoreRepo.addIgnore(playerId, targetId);
	}
	public void unignore(Player player, Player target) {
		UUID playerId = player.getUniqueId();
		UUID targetId = target.getUniqueId();
		
		Set<UUID> list = ignoreCache.get(playerId);
		
		if (list == null || !list.contains(targetId)) {
			player.sendMessage(plugin.format(null, config.getString("Teleportation.notIgnoring"), null));
			return;
		}
		
		list.remove(targetId);
		player.sendMessage(plugin.format(target, config.getString("Teleportation.ignoreRevoke"),
				Map.of(
						"player",target.getName())));
		ignoreRepo.removeIgnore(playerId, targetId);
	}
	public boolean isIgnoring(UUID player, UUID target) {
		Set<UUID> list = ignoreCache.get(player);
		return list != null && list.contains(target);
	}
	public void reload() {
		config = plugin.getTeleportConfig().get();
	}
	public Set<UUID> getIgnored(UUID player) {
		return ignoreCache.getOrDefault(player, Collections.emptySet());
	}
}
