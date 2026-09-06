package org.voxelware.coretuff.Features.Teleportation.Handler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class ExecutionHandler {
	
	private final Plugin plugin;
	private final boolean folia;
	
	public ExecutionHandler(Plugin plugin, boolean folia) {
		this.plugin = plugin;
		this.folia = folia;
	}
	
	public void execute(Location location, Runnable task) {
		if (folia) {
			Bukkit.getRegionScheduler().execute(plugin, location, task);
		} else {
			Bukkit.getScheduler().runTask(plugin, task);
		}
	}
}