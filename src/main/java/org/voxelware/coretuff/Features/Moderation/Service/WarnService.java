package org.voxelware.coretuff.Features.Moderation.Service;

import org.bukkit.Bukkit;
import org.voxelware.coretuff.Features.Moderation.ModIO.DurationParser;
import org.voxelware.coretuff.Features.Moderation.ModIO.ModerationMessages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WarnService {

	private final Map<UUID, List<WarnEntry>> warns = new HashMap<>();

	public int addWarn(UUID uuid, int points) {
		List<WarnEntry> entries = warns.computeIfAbsent(uuid, ignored -> new ArrayList<>());
		entries.add(new WarnEntry(points, System.currentTimeMillis()));
		return getWarns(uuid);
	}

	public int getWarns(UUID uuid) {
		List<WarnEntry> entries = warns.get(uuid);
		if (entries == null || entries.isEmpty()) {
			return 0;
		}

		long intervalMillis = getDecayIntervalMillis();
		int decayAmount = getDecayAmount();
		long now = System.currentTimeMillis();
		int total = 0;

		entries.removeIf(entry -> getRemainingPoints(entry, now, intervalMillis, decayAmount) <= 0);
		for (WarnEntry entry : entries) {
			total += getRemainingPoints(entry, now, intervalMillis, decayAmount);
		}

		if (entries.isEmpty()) {
			warns.remove(uuid);
		}

		return total;
	}

	public int parseSeverity(String input) {
		return switch (input.toLowerCase()) {
			case "low" -> ModerationMessages.getInt("moderation.warn-points.low", 1);
			case "medium" -> ModerationMessages.getInt("moderation.warn-points.medium", 2);
			case "high" -> ModerationMessages.getInt("moderation.warn-points.high", 3);
			default -> -1;
		};
	}

	public void handlePunishment(UUID uuid, String name) {
		int total = getWarns(uuid);
		int tempBanPoints = ModerationMessages.getInt("moderation.auto-punishments.tempban-points", 3);
		int banPoints = ModerationMessages.getInt("moderation.auto-punishments.ban-points", 6);
		String tempBanDuration = ModerationMessages.get(
				"moderation.auto-punishments.tempban-duration",
				"1d"
		);
		String tempBanReason = ModerationMessages.get(
				"moderation.auto-punishments.tempban-reason",
				"Too many warnings"
		);
		String banReason = ModerationMessages.get(
				"moderation.auto-punishments.ban-reason",
				"Too many warnings"
		);

		if (total >= banPoints) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
					"ban " + name + " " + banReason);
			return;
		}

		if (total >= tempBanPoints) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
					"tempban " + name + " " + tempBanDuration + " " + tempBanReason);
		}
	}

	private int getDecayAmount() {
		return Math.max(0, ModerationMessages.getInt("moderation.warn-decay.amount", 3));
	}

	private long getDecayIntervalMillis() {
		String configured = ModerationMessages.get("moderation.warn-decay.interval", "1mo");
		try {
			return DurationParser.parse(configured);
		} catch (IllegalArgumentException ignored) {
			return DurationParser.parse("1mo");
		}
	}

	private int getRemainingPoints(WarnEntry entry, long now, long intervalMillis, int decayAmount) {
		if (decayAmount <= 0 || intervalMillis <= 0L) {
			return entry.points();
		}

		long intervalsElapsed = Math.max(0L, (now - entry.createdAt()) / intervalMillis);
		long decayed = intervalsElapsed * decayAmount;
		return (int) Math.max(0L, entry.points() - decayed);
	}

	private record WarnEntry(int points, long createdAt) {}
}
