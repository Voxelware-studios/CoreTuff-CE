package org.voxelware.coretuff.Features.Utility.commandShorthand;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.commandExecuter.BaseCommand;

import java.util.Collections;
import java.util.List;

public class noonCommand extends BaseCommand {

	private final CoreTuff plugin;

	public noonCommand(CoreTuff plugin) {
		this.plugin = plugin;
	}

	@Override
	protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			return Bukkit.getWorlds().stream().map(World::getName).toList();
		}
		return Collections.emptyList();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (args.length > 1) {
			return false;
		}

		if (!sender.hasPermission("coretuff.time.noon")) {
			Player player = sender instanceof Player p ? p : null;
			sender.sendMessage(plugin.format(player, plugin.getCoreString("nopermission", "&cYou don't have permission to use this command."), null));
			return true;
		}

		final World targetWorld;
		if (args.length == 1) {
			targetWorld = Bukkit.getWorld(args[0]);
			if (targetWorld == null) {
				sender.sendMessage(createErrorComponent("World '%s' doesn't exist.", args[0]));
				return true;
			}
		} else {
			targetWorld = null;
		}

		Runnable setTimeTask = () -> {
			if (targetWorld != null) {
				targetWorld.setTime(6000L);
			} else {
				for (World world : Bukkit.getWorlds()) {
					world.setTime(6000L);
				}
			}
		};

		if (plugin.isFolia()) {
			Bukkit.getGlobalRegionScheduler().run(plugin, task -> setTimeTask.run());
		} else {
			setTimeTask.run();
		}

		FileConfiguration config = plugin.getUtilityConfig().get();
		String msg = config != null ? config.getString("Time_Shorthand.noon", "&aTime set to Noon.") : "&aTime set to Noon.";
		Player player = sender instanceof Player p ? p : null;
		sender.sendMessage(plugin.format(player, msg, null));
		return true;
	}
}

