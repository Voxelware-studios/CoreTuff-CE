package org.voxelware.coretuff.Features.Teleportation.Listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.voxelware.coretuff.Utility.CoreTuffProvider;

public class DeathListener implements Listener {
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		CoreTuffProvider.getTpManager()
				.setLastLocation(player);
	}
}