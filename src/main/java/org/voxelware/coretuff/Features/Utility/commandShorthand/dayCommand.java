package org.voxelware.coretuff.Features.Utility.commandShorthand;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public class dayCommand extends PlayerCommand {
	
	private final CoreTuff plugin;
	
	public dayCommand(CoreTuff plugin) {
		this.plugin = plugin;
	}
	
	@Override
	protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
		return Collections.emptyList();
	}
	
	@Override
	public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NonNull @NotNull String[] args) {
		FileConfiguration config = plugin.getUtilityConfig().get();
		if (args.length == 0) {
			
			
			if (!player.hasPermission("coretuff.time.day")) {
				player.sendMessage(plugin.format(player, plugin.getCoreString("nopermission"), null));
				return false;
			}
			for (World world : Bukkit.getWorlds()) {
                world.setTime(1000);
            }
			return false;
		} else {
            return false;
        }
	}
}
