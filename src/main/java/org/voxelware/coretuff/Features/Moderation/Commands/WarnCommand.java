package org.voxelware.coretuff.Features.Moderation.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.Features.Moderation.ModIO.ModerationMessages;
import org.voxelware.coretuff.Features.Moderation.Service.WarnService;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.Arrays;
import java.util.List;

public class WarnCommand extends PlayerCommand {

	private final WarnService warnService;

	public WarnCommand(WarnService warnService) {
		this.warnService = warnService;
	}

	@Override
	public boolean onCommand(@NotNull Player sender,
	                         @NotNull Command command,
	                         @NotNull String label,
	                         @NotNull String[] args) {

		if (!sender.hasPermission("coretuff.moderation.warn")) {
			ModerationMessages.send(sender,
					"moderation.messages.no-permission",
					"&cYou do not have permission to use this moderation command.");
			return true;
		}

		if (args.length < 3) {
			ModerationMessages.send(sender,
					"moderation.commands.warn.usage",
					"&cUsage: /warn <player> <low|medium|high> <reason>");
			return true;
		}

		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			ModerationMessages.send(sender,
					"moderation.messages.player-not-found",
					"&cPlayer not found.");
			return true;
		}

		if (!ModerationMessages.canPunish(sender, target)) {
			return true;
		}

		int severity = warnService.parseSeverity(args[1]);
		if (severity == -1) {
			ModerationMessages.send(sender,
					"moderation.commands.warn.invalid-severity",
					"&cInvalid severity. Use: low, medium, high");
			return true;
		}

		String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
		int total = warnService.addWarn(target.getUniqueId(), severity);
		java.util.Map<String, String> placeholders = java.util.Map.of(
				"reason", reason,
				"severity", args[1].toLowerCase(),
				"points", String.valueOf(total)
		);

		ModerationMessages.sendLines(
				target,
				target,
				"moderation.commands.warn.target-message",
				java.util.List.of(
						"&cYou have been warned.",
						"&7Reason: &f{reason}",
						"&7Severity: &f{severity}",
						"&7Total points: &f{points}"
				),
				placeholders
		);

		ModerationMessages.send(sender,
				"moderation.commands.warn.sender-success",
				"&aWarn added. Total points: {points}",
				java.util.Map.of("points", String.valueOf(total)));
		warnService.handlePunishment(target.getUniqueId(), target.getName());
		return true;
	}

	@Override
	protected List<String> generateCompletions(org.bukkit.command.CommandSender sender,
	                                           String label,
	                                           String[] args) {
		
		if (args.length == 1) {
			return Bukkit.getOnlinePlayers()
					.stream()
					.map(Player::getName)
					.toList();
		}
		
		if (args.length == 2) {
			return List.of(
					"low",
					"medium",
					"high"
			);
		}
		
		if (args.length >= 3 && args[args.length - 1].isBlank()) {
			return List.of("<reason>");
		}
		
		return List.of();
	}
}
