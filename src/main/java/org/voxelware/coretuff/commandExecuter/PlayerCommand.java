package org.voxelware.coretuff.commandExecuter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class PlayerCommand extends BaseCommand {
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
	                         @NotNull String @NotNull [] args) {
		if (sender instanceof Player player) {
			return onCommand(player, command, label, args);
		}
		sender.sendMessage(createErrorComponent(ERR_NOT_PLAYER));
		return true;
	}
	
	public abstract boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NotNull String[] args);
	protected List<String> suggest(String... suggestions) {
		return List.of(suggestions);
	}
}
