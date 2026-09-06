package org.voxelware.coretuff.Features.Teleportation.Commands;

import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Utility.CoreTuffProvider;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class TpDenyCommand extends PlayerCommand {

	private final CoreTuff plugin;

	public TpDenyCommand(CoreTuff plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(Player player, Command command, String label, String[] args) {
		FileConfiguration utilityConfig = plugin.getUtilityConfig().get();
		if (!player.hasPermission("coretuff.teleportation.tpdeny")) {
			player.sendMessage(plugin.format(player, plugin.getCoreString("nopermission"), null));
			return true;
		}

		CoreTuffProvider.getTpManager().denyRequest(player);
		return true;
	}

	@Override
	protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
		return List.of();
	}
}
