package org.voxelware.coretuff.Features.Teleportation.Commands;

import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Utility.CoreTuffProvider;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class TpAskCommand extends PlayerCommand {

	private final CoreTuff plugin;

	public TpAskCommand(CoreTuff plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(Player player, Command command, String label, String[] args) {
		FileConfiguration utilityConfig = plugin.getUtilityConfig().get();

		if (args.length != 1) {
			player.sendMessage(plugin.format(player, utilityConfig.getString("cmdUse1"), Map.of("command", label)));
			return true;
		}

		if (!player.hasPermission("coretuff.teleportation.tpask")) {
			player.sendMessage(plugin.format(player, plugin.getCoreString("nopermission"), null));
			return true;
		}

		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			player.sendMessage(plugin.format(player, utilityConfig.getString("playerOffline"), null));
			return true;
		}

		CoreTuffProvider.getTpManager().sendRequest(player, target, false);
		return true;
	}

	@Override
	protected List<String> generateCompletions(org.bukkit.command.CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
		}

		return List.of();
	}
}
