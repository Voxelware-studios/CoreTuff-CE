package org.voxelware.coretuff.Features.Teleportation.Listener;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Teleportation.DelayedTeleporter;

public class TeleportCancelListener implements Listener {
	
	private final DelayedTeleporter delayedTeleporter;
	private final CoreTuff plugin;
	
	public TeleportCancelListener(DelayedTeleporter delayedTeleporter, CoreTuff plugin) {
		this.delayedTeleporter = delayedTeleporter;
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		FileConfiguration config = plugin.getTeleportConfig().get();
		if (!(event.getEntity() instanceof Player player)) return;
		if (!delayedTeleporter.isTeleporting(player)) return;
		
		delayedTeleporter.cancel(player);
		player.sendMessage(plugin.format(player, config.getString("Teleportation.tpCancDamage"), null));
	}
}
