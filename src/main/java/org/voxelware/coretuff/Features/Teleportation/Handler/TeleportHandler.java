package org.voxelware.coretuff.Features.Teleportation.Handler;

import java.util.concurrent.CompletableFuture;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface TeleportHandler {
	CompletableFuture<Boolean> teleport(Player paramPlayer, Location paramLocation);
}