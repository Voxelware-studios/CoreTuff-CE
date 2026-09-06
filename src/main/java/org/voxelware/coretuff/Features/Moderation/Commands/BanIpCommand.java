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

public class BanIpCommand extends PlayerCommand {
	
	private final ModerationService service;
	
	public BanIpCommand(ModerationService service) {
		this.service = service;
	}
	
	@Override
	public boolean onCommand(@NotNull Player sender,
	                         @NotNull Command command,
	                         @NotNull String label,
	                         @NotNull String[] args) {
		
		if (!sender.hasPermission("coretuff.moderation.banip")) {
			ModerationMessages.send(sender,
					"moderation.messages.no-permission",
					"&cYou do not have permission to use this moderation command.");
			return true;
		}
		
		if (args.length < 2) {
			ModerationMessages.send(sender,
					"moderation.commands.ban-ip.usage",
					"&cUsage: /ban-ip <player> <reason>");
			return true;
		}
		
		Player target = Bukkit.getPlayer(args[0]);
		
		if (target == null || target.getAddress() == null) {
			ModerationMessages.send(sender,
					"moderation.messages.player-not-found-or-no-ip",
					"&cPlayer not found or no IP.");
			return true;
		}

		if (!ModerationMessages.canPunish(sender, target)) {
			return true;
		}
		
		String ip = target.getAddress().getAddress().getHostAddress();
		String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
		
		service.ipBan(ip, target.getName(), reason, sender.getName());
		
		ModerationMessages.send(sender,
				"moderation.commands.ban-ip.success",
				"&aIP banned {player}",
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
