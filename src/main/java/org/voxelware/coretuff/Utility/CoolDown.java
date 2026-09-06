package org.voxelware.coretuff.Utility;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CoolDown {
	private static final Map<String, Map<UUID, Long>> cooldowns = new ConcurrentHashMap<>();
	public static boolean isOnCooldown(String key, UUID uuid, long cooldownMillis) {
		cooldowns.putIfAbsent(key, new HashMap<>());
		
		Map<UUID, Long> map = cooldowns.get(key);
		
		if (!map.containsKey(uuid)) return false;
		
		long last = map.get(uuid);
		return (System.currentTimeMillis() - last) < cooldownMillis;
	}
	
	public static long getRemaining(String key, UUID uuid, long cooldownMillis) {
		Map<UUID, Long> map = cooldowns.getOrDefault(key, Map.of());
		
		if (!map.containsKey(uuid)) return 0;
		
		long last = map.get(uuid);
		long remaining = cooldownMillis - (System.currentTimeMillis() - last);
		
		return Math.max(remaining, 0);
	}

	public static boolean isOnCooldown(String key, Player player, long cooldownMillis) {
		if (shouldBypass(player) || cooldownMillis <= 0L) {
			return false;
		}

		return isOnCooldown(key, player.getUniqueId(), cooldownMillis);
	}

	public static long getRemaining(String key, Player player, long cooldownMillis) {
		if (shouldBypass(player) || cooldownMillis <= 0L) {
			return 0L;
		}

		return getRemaining(key, player.getUniqueId(), cooldownMillis);
	}
	
	public static void setCooldown(String key, UUID uuid) {
		cooldowns.putIfAbsent(key, new HashMap<>());
		cooldowns.get(key).put(uuid, System.currentTimeMillis());
	}

	public static void setCooldown(String key, Player player) {
		if (shouldBypass(player)) {
			return;
		}

		setCooldown(key, player.getUniqueId());
	}

	public static boolean shouldBypass(Player player) {
		return player != null && player.isOp();
	}
}
