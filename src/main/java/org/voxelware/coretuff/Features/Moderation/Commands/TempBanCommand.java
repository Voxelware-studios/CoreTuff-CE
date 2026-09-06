package org.voxelware.coretuff.Features.Moderation.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.Features.Moderation.ModIO.DurationParser;
import org.voxelware.coretuff.Features.Moderation.ModIO.ModerationMessages;
import org.voxelware.coretuff.Features.Moderation.Service.ModerationService;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.Arrays;
import java.util.List;

public class TempBanCommand extends PlayerCommand {
	
	private final ModerationService moderationService;
	
	public TempBanCommand(ModerationService moderationService) {
		this.moderationService = moderationService;
	}
	
	@Override
	public boolean onCommand(@NotNull Player sender,
	                         @NotNull Command command,
	                         @NotNull String label,
	                         @NotNull String[] args) {
		
		if (!sender.hasPermission("coretuff.moderation.tempban")) {
			ModerationMessages.send(sender,
					"moderation.messages.no-permission",
					"&cYou do not have permission to use this moderation command.");
			return true;
		}
		
		if (args.length < 3) {
			ModerationMessages.send(sender,
					"moderation.commands.tempban.usage",
					"&cUsage: /tempban <player> <time> <reason>");
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
		
		long duration;
		try {
			duration = DurationParser.parse(args[1]);
		} catch (IllegalArgumentException exception) {
			ModerationMessages.send(sender,
					"moderation.commands.tempban.invalid-duration",
					"&c{message}",
					java.util.Map.of("message", exception.getMessage()));
			return true;
		}
		
		String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
		
		moderationService.tempBan(
				target.getUniqueId(),
				target.getName(),
				reason,
				duration,
				sender.getName()
		);
		
		ModerationMessages.send(sender,
				"moderation.commands.tempban.success",
				"&aTemp banned {player}",
				java.util.Map.of("player", target.getName()));
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
					"1h",
					"1d",
					"7d",
					"30d"
			);
		}
		
		if (args.length >= 3 && args[args.length - 1].isBlank()) {
			return List.of("<reason>");
		}
		
		return List.of();
	}
}
