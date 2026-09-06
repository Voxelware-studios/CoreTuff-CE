package org.voxelware.coretuff.Features.Utility.commandShorthand;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class gmcCommand extends PlayerCommand {
	
	private final CoreTuff plugin;
	
	public gmcCommand(CoreTuff plugin) {
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
			
			if (!player.hasPermission("coretuff.gamemode.gmc")) {
				player.sendMessage(plugin.format(player, plugin.getCoreString("nopermission"), null));
				return true;
			}
				player.setGameMode(GameMode.CREATIVE);
				player.sendMessage(plugin.format(player, config.getString("Gamemode_Shorthand.creative"), null));
				return true;
		}
		if (args.length == 1) {
			
			if(!player.hasPermission("coretuff.gamemode.gmc.others")) {
				player.sendMessage(plugin.format(player, plugin.getCoreString("nopermission"), null));
				return true;
			}
			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				player.sendMessage(plugin.format(player, config.getString("playerOffline"), null));
				return true;
			}
			target.setGameMode(GameMode.CREATIVE);
			
			player.sendMessage(plugin.format(target, config.getString("Gamemode_Shorthand.gamemodeSet"), Map.of(
					"player", target.getName(),
					"gamemode", target.getGameMode().name()
			)));
			target.sendMessage(plugin.format(player ,config.getString("Gamemode_Shorthand.gamemodeChange"),
					Map.of(
							"player", player.getName(),
							"gamemode", target.getGameMode().name()
					)));
			return true;
		}
		return false;
	}
}
