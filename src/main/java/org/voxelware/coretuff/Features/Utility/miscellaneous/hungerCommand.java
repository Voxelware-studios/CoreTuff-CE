package org.voxelware.coretuff.Features.Utility.miscellaneous;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.voxelware.coretuff.CoreTuff;

import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.List;
import java.util.Map;

public class hungerCommand extends PlayerCommand {
	private final CoreTuff plugin;
	
	public hungerCommand(CoreTuff plugin) {
		this.plugin = plugin;
	}
	
	@Override
	protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
		return List.of();
	}
	
	@Override
	public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NonNull @NotNull String[] args) {
		FileConfiguration config = plugin.getUtilityConfig().get();
		if (args.length == 0) {
		if(!player.hasPermission("coretuff.utility.feed")) {
			player.sendMessage(plugin.format(player, plugin.getCoreString("nopermission"),null));
		return true;
		}
			player.setFoodLevel(20);
			player.setSaturation(20);
			player.sendMessage(plugin.format(player, config.getString("Feed.filled"), null));
		    return true;
		}
		if (args.length == 1) {
			if(!player.hasPermission("coretuff.utility.feed.others")) {
				player.sendMessage(plugin.format(player, plugin.getCoreString("nopermission"),null));
				return true;
			}
			Player target = Bukkit.getPlayer(args[0]);
			if(target == null) {
				player.sendMessage(plugin.format(player, config.getString("playerOffline"),null));
				return true;
			}
			target.setFoodLevel(20);
			target.setSaturation(20);
			player.sendMessage(plugin.format(target, config.getString("Feed.feedOther"),
					Map.of(
							"player", target.getName()
					)));
			target.sendMessage(plugin.format(player, config.getString("Feed.feedTarget"),
					Map.of(
							"player", player.getName()
					)));
			return true;
		}
		return false;
	}
}
