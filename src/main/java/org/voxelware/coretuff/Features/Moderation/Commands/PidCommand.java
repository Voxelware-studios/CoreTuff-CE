package org.voxelware.coretuff.Features.Moderation.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.Features.Moderation.FormattEngine.DurationFormatter;
import org.voxelware.coretuff.Features.Moderation.ModIO.ModerationMessages;
import org.voxelware.coretuff.Features.Moderation.PunishmentEngine.PunishmentRecord;
import org.voxelware.coretuff.Features.Moderation.Service.ModerationService;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PidCommand extends PlayerCommand {

	private static final int PAGE_SIZE = 5;

	private final ModerationService service;

	public PidCommand(ModerationService service) {
		this.service = service;
	}

	@Override
	public boolean onCommand(@NotNull Player sender,
	                         @NotNull Command command,
	                         @NotNull String label,
	                         @NotNull String[] args) {

		if (!sender.hasPermission("coretuff.moderation.pid")) {
			ModerationMessages.send(sender,
					"moderation.messages.no-permission",
					"&cYou do not have permission to use this moderation command.");
			return true;
		}

		if (args.length == 0) {
			showRecentPunishments(sender, 1);
			return true;
		}

		if (args.length == 1 && isPageArgument(args[0])) {
			showRecentPunishments(sender, Integer.parseInt(args[0]));
			return true;
		}

		if (args.length < 2) {
			ModerationMessages.send(sender,
					"moderation.commands.pid.usage",
					"&cUsage: /pid [page] or /pid <view|pardon> <id>");
			return true;
		}

		String sub = args[0].toLowerCase();
		switch (sub) {
			case "view" -> {
				showPunishment(sender, args[1]);
				return true;
			}
			case "pardon" -> {
				if (args.length < 3) {
					ModerationMessages.send(sender,
							"moderation.commands.pid.pardon-usage",
							"&cUsage: /pid pardon <id> <reason>");
					return true;
				}

				String id = args[1];
				String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
				service.pardonById(id, reason, sender.getName());
				ModerationMessages.send(sender,
						"moderation.commands.pid.pardon-success",
						"&aPunishment {id} pardoned.",
						Map.of("id", id));
				return true;
			}
			default -> {
				ModerationMessages.send(sender,
						"moderation.commands.pid.unknown-subcommand",
						"&cUnknown subcommand.");
				return true;
			}
		}
	}

	private void showRecentPunishments(Player sender, int page) {
		if (page < 1) {
			ModerationMessages.send(sender,
					"moderation.commands.pid.invalid-page",
					"&cInvalid page number.");
			return;
		}

		List<PunishmentRecord> records = service.getRecentPunishments(page * PAGE_SIZE);
		int startIndex = (page - 1) * PAGE_SIZE;
		if (startIndex >= records.size()) {
			ModerationMessages.send(sender,
					"moderation.commands.pid.invalid-page",
					"&cInvalid page number.");
			return;
		}

		List<PunishmentRecord> pageRecords = records.stream()
				.skip(startIndex)
				.limit(PAGE_SIZE)
				.toList();

		ModerationMessages.send(sender,
				"moderation.commands.pid.recent-title",
				"&6Recent Punishments: &7(Page {page})",
				Map.of("page", String.valueOf(page)));

		for (PunishmentRecord record : pageRecords) {
			String player = record.getName() != null ? record.getName() : record.getIp();
			String status = record.isActive()
					? ModerationMessages.get("moderation.commands.pid.status-active", "&aACTIVE")
					: ModerationMessages.get("moderation.commands.pid.status-pardoned", "&cPARDONED");

			ModerationMessages.sendBody(sender,
					sender,
					"moderation.commands.pid.recent-entry",
					"&e#{id} &7- &f{player} &7- &6{type} &7- {status}",
					Map.of(
							"id", record.getId(),
							"player", player == null ? "Unknown" : player,
							"type", record.getType(),
							"status", status
					));
		}
	}

	private void showPunishment(Player sender, String punishmentId) {
		PunishmentRecord record = service.getPunishmentById(punishmentId);
		if (record == null) {
			ModerationMessages.send(sender,
					"moderation.commands.pid.no-punishment-found",
					"&cNo punishment found.");
			return;
		}

		String player = record.getName() != null ? record.getName() : record.getIp();
		String status = record.isActive()
				? ModerationMessages.get("moderation.commands.pid.status-active", "&aACTIVE")
				: ModerationMessages.get("moderation.commands.pid.status-pardoned", "&cPARDONED");

		ModerationMessages.send(sender,
				"moderation.commands.pid.view.title",
				"&6Punishment #{id}",
				Map.of("id", record.getId()));
		ModerationMessages.sendBody(sender,
				sender,
				"moderation.commands.pid.view.type",
				"&eType: &f{type}",
				Map.of("type", record.getType()));
		ModerationMessages.sendBody(sender,
				sender,
				"moderation.commands.pid.view.player",
				"&ePlayer: &f{player}",
				Map.of("player", player == null ? "Unknown" : player));
		ModerationMessages.sendBody(sender,
				sender,
				"moderation.commands.pid.view.moderator",
				"&eModerator: &f{moderator}",
				Map.of("moderator", record.getModerator() == null ? "Unknown" : record.getModerator()));
		ModerationMessages.sendBody(sender,
				sender,
				"moderation.commands.pid.view.reason",
				"&eReason: &f{reason}",
				Map.of("reason", record.getReason() == null ? "Unknown" : record.getReason()));

		if (record.getIp() != null && !record.getIp().isBlank()) {
			ModerationMessages.sendBody(sender,
					sender,
					"moderation.commands.pid.view.ip",
					"&eIP: &f{ip}",
					Map.of("ip", record.getIp()));
		}

		ModerationMessages.sendBody(sender,
				sender,
				"moderation.commands.pid.view.status",
				"&eStatus: &f{status}",
				Map.of("status", status));

		boolean tempban = record.getType().equalsIgnoreCase("TEMPBAN");
		if (tempban && record.isActive()) {
			long remaining = record.getExpiresAt() - System.currentTimeMillis();
			if (remaining > 0) {
				ModerationMessages.sendBody(sender,
						sender,
						"moderation.commands.pid.view.remaining",
						"&eRemaining: &f{remaining}",
						Map.of("remaining", DurationFormatter.format(remaining)));
			}
		}

		if (record.isPermanent()) {
			ModerationMessages.sendBody(sender,
					sender,
					"moderation.commands.pid.view.duration",
					"&eDuration: &f{duration}",
					Map.of("duration", "Permanent"));
		}

		if (!record.isActive()) {
			String pardonReason = record.getPardonReason();
			String pardonedBy = record.getPardonedBy();

			if (tempban && "Punishment expired".equalsIgnoreCase(pardonReason)) {
				pardonReason = "Duration End";
				pardonedBy = "Server";
			}

			ModerationMessages.sendBody(sender,
					sender,
					"moderation.commands.pid.view.pardon-reason",
					"&ePardon Reason: &f{pardonReason}",
					Map.of("pardonReason", pardonReason == null ? "Unknown" : pardonReason));
			ModerationMessages.sendBody(sender,
					sender,
					"moderation.commands.pid.view.pardoned-by",
					"&ePardoned By: &f{pardonedBy}",
					Map.of("pardonedBy", pardonedBy == null ? "Unknown" : pardonedBy));
		}
	}

	@Override
	protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			return List.of("1", "2", "view", "pardon");
		}

		if (args.length == 2 && isPidSubcommand(args[0])) {
			return service.getRecentPunishments(20).stream()
					.sorted(Comparator.comparingLong(PunishmentRecord::getCreatedAt).reversed())
					.map(PunishmentRecord::getId)
					.distinct()
					.toList();
		}

		if (args.length >= 3
				&& args[0].equalsIgnoreCase("pardon")
				&& args[args.length - 1].isBlank()) {
			return List.of("<reason>");
		}

		return List.of();
	}

	private boolean isPidSubcommand(String value) {
		return value.equalsIgnoreCase("view")
				|| value.equalsIgnoreCase("pardon");
	}

	private boolean isPageArgument(String value) {
		try {
			return Integer.parseInt(value) > 0;
		} catch (NumberFormatException ignored) {
			return false;
		}
	}
}
