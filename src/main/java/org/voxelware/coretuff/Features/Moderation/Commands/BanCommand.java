package org.voxelware.coretuff.Features.Moderation.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.Features.Moderation.ModIO.ModerationMessages;
import org.voxelware.coretuff.Features.Moderation.Service.ModerationService;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.Arrays;
import java.util.List;

public class BanCommand extends PlayerCommand {
	
	private final ModerationService moderationService;
	
	public BanCommand(ModerationService moderationService) {
		this.moderationService = moderationService;
	}
	
	@Override
	public boolean onCommand(@NotNull Player sender,
	                         @NotNull Command command,
	                         @NotNull String label,
	                         @NotNull String[] args) {
		
		if (!sender.hasPermission("coretuff.moderation.ban")) {
			ModerationMessages.send(sender,
					"moderation.messages.no-permission",
					"&cYou do not have permission to use this moderation command.");
			return true;
		}
		
		if (args.length < 2) {
			ModerationMessages.send(sender,
					"moderation.commands.ban.usage",
					"&cUsage: /ban <player> <reason>");
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
		
		String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		
		moderationService.ban(
				target.getUniqueId(),
				target.getName(),
				reason,
				sender.getName()
		);
		
		ModerationMessages.send(sender,
				"moderation.commands.ban.success",
				"&aBanned {player}",
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
		
		if (args.length >= 2 && args[args.length - 1].isBlank()) {
			return List.of("<reason>");
		}
		
		return List.of();
	}
}
