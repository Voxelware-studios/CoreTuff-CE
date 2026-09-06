package org.voxelware.coretuff.Features.Teleportation.Commands;

import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Utility.CoreTuffProvider;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.World;

import java.util.List;
import java.util.Locale;

public class RtpCommand extends PlayerCommand {

	private final CoreTuff plugin;

	public RtpCommand(CoreTuff plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(Player player, Command command, String label, String[] args) {
		FileConfiguration utilityConfig = plugin.getUtilityConfig().get();
		if (args.length == 0) {
			if (!player.hasPermission("coretuff.teleportation.rtp")) {
				player.sendMessage(plugin.format(player, plugin.getCoreString("nopermission"), null));
				return true;
			}

			CoreTuffProvider.getRtpService().randomTeleport(player);
			return true;
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("world")) {
			World targetWorld = Bukkit.getWorld(args[1]);
			if (targetWorld == null) {
				player.sendMessage(createErrorComponent("World not found."));
				return true;
			}

			String worldPermission =
					"coretuff.teleportation.rtp." + targetWorld.getName();
			String normalizedWorldPermission =
					"coretuff.teleportation.rtp." + targetWorld.getName().toLowerCase(Locale.ROOT);
			if (!player.hasPermission(worldPermission)
					&& !player.hasPermission(normalizedWorldPermission)
					&& !player.hasPermission("coretuff.teleportation.rtp.*")) {
				player.sendMessage(plugin.format(player, plugin.getCoreString("nopermission"), null));
				return true;
			}

			CoreTuffProvider.getRtpService().randomTeleport(player, targetWorld);
			return true;
		}

		player.sendMessage(createErrorComponent("Usage: /%s [world <world>]", label));
		return true;
	}

	@Override
	protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			return List.of("world");
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("world")) {
			return Bukkit.getWorlds().stream()
					.map(World::getName)
					.toList();
		}

		return List.of();
	}
}
