package org.voxelware.coretuff.Features.Teleportation.Handler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public class FoliaTeleportHandler implements TeleportHandler {

	private final Plugin plugin;

	public FoliaTeleportHandler(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public CompletableFuture<Boolean> teleport(Player player, Location target) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();

		if (target.getWorld() == null) {
			future.complete(false);
			return future;
		}

		target.getWorld().getChunkAtAsync(target).thenAccept(chunk ->
				Bukkit.getRegionScheduler().execute(plugin, target, () -> {
					if (!player.isOnline()) {
						future.complete(false);
						return;
					}

					player.teleportAsync(target).whenComplete((success, throwable) -> {
						if (throwable != null) {
							future.complete(false);
							return;
						}

						future.complete(Boolean.TRUE.equals(success));
					});
				})
		).exceptionally(ex -> {
			future.complete(false);
			return null;
		});

		return future;
	}
}
