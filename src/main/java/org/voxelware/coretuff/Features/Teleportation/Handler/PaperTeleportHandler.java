package org.voxelware.coretuff.Features.Teleportation.Handler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public class PaperTeleportHandler implements TeleportHandler {

	private final Plugin plugin;

	public PaperTeleportHandler(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public CompletableFuture<Boolean> teleport(Player player, Location target) {
		CompletableFuture<Boolean> future = new CompletableFuture<>();

		Bukkit.getScheduler().runTask(plugin, () -> {
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
		});

		return future;
	}
}
