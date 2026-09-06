package org.voxelware.coretuff.Features.Teleportation;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Teleportation.Handler.ExecutionHandler;


import java.util.Random;
import java.util.Set;

public class RtpService {

	private static final String RTP_COOLDOWN = "rtp";
	private static final long RTP_DELAY_SECONDS = 5L;
	private static final int NETHER_SCAN_CEILING = 120;

	private final CoreTuff plugin;
	private final TpManager manager;
	private final ExecutionHandler executor;
	private FileConfiguration config;
	private final Random random = new Random();
	private final int radius = 5000;
	private final int maxAttempts = 30;
	private final Set<Biome> blockedBiomes = Set.of(
			Biome.OCEAN,
			Biome.DEEP_OCEAN,
			Biome.RIVER,
			Biome.FROZEN_OCEAN,
			Biome.DEEP_FROZEN_OCEAN
	);

	public RtpService(CoreTuff plugin, TpManager manager, ExecutionHandler executor) {
		this.plugin = plugin;
		this.manager = manager;
		this.executor = executor;
		reload();
	}

	public void randomTeleport(Player player) {
		randomTeleport(player, player.getWorld());
	}

	public void randomTeleport(Player player, World world) {
		if (manager.isOnCooldown(player, RTP_COOLDOWN)) {
			player.sendMessage(plugin.format(player, config.getString("Teleportation.rtpOnCooldown"), null));
			return;
		}

		player.sendMessage(plugin.format(player, config.getString("Teleportation.rtpLocSearch"), null));
		attempt(player, world, 0);
	}

	private void attempt(Player player, World world, int attempt) {
		if (attempt >= maxAttempts) {
			player.sendMessage(plugin.format(player, config.getString("Teleportation.rtpNoSafeLoc"), null));
			return;
		}

		int x = random.nextInt(radius * 2) - radius;
		int z = random.nextInt(radius * 2) - radius;
		Location base = new Location(world, x, 0, z);

		int chunkX = x >> 4;
		int chunkZ = z >> 4;

		boolean generatedOnly =
				config.getBoolean(
						"Teleportation.rtpGeneratedChunksOnly",
						false
				);

		world.getChunkAtAsync(
				chunkX,
				chunkZ,
				!generatedOnly,
				true
		).thenAccept(chunk -> {

			if (chunk == null) {

				attempt(
						player,
						world,
						attempt + 1
				);

				return;
			}

			chunk.addPluginChunkTicket(
					plugin
			);

			executor.execute(base, () -> {

				if (!player.isOnline()) {

					chunk.removePluginChunkTicket(
							plugin
					);

					return;
				}

				Location location =
						findSafeLocation(
								world,
								x,
								z
						);

				if (!isBiomeSafe(world, x, z)
						|| location == null) {

					chunk.removePluginChunkTicket(
							plugin
					);

					attempt(
							player,
							world,
							attempt + 1
					);

					return;
				}

				manager.teleport(
						player,
						location,
						RTP_DELAY_SECONDS
				).whenComplete((success, throwable) ->

						executor.execute(location, () -> {

							chunk.removePluginChunkTicket(
									plugin
							);

							if (!player.isOnline()) {
								return;
							}

							if (throwable != null
									|| !Boolean.TRUE.equals(success)) {

								player.sendMessage(
										plugin.format(
												player,
												config.getString(
														"Teleportation.rtpTeleportFailed"
												),
												null
										)
								);

								return;
							}

							manager.setCooldown(
									player,
									RTP_COOLDOWN
							);

							player.sendMessage(
									plugin.format(
											player,
											config.getString(
													"Teleportation.rtpTeleported"
											),
											null
									)
							);
						})
				);
			});

		}).exceptionally(ex -> {

			attempt(
					player,
					world,
					attempt + 1
			);

			return null;
		});
	}

	private boolean isBiomeSafe(World world, int x, int z) {
		return !blockedBiomes.contains(world.getBiome(x, world.getMinHeight(), z));
	}

	private Location findSafeLocation(World world, int x, int z) {
		if (world.getEnvironment() == World.Environment.NETHER) {
			int startY = Math.min(world.getMaxHeight() - 2, NETHER_SCAN_CEILING);
			for (int y = startY; y > world.getMinHeight(); y--) {
				Location candidate = new Location(world, x + 0.5, y, z + 0.5);
				if (isSafe(candidate) && !isNetherRoof(candidate)) {
					return candidate;
				}
			}
			return null;
		}

		int y = world.getHighestBlockYAt(x, z) + 1;
		Location candidate = new Location(world, x + 0.5, y, z + 0.5);
		return isSafe(candidate) ? candidate : null;
	}

	private boolean isSafe(Location location) {
		Block feet = location.getBlock();
		Block head = location.clone().add(0, 1, 0).getBlock();
		Block below = location.clone().add(0, -1, 0).getBlock();

		if (isHazard(feet.getType()) || isHazard(head.getType())) {
			return false;
		}

		if (!isPassable(feet) || !isPassable(head)) {
			return false;
		}

		Material ground = below.getType();
		return ground.isSolid() && !isHazard(ground);
	}

	private boolean isPassable(Block block) {
		Material type = block.getType();
		return type.isAir() || block.isPassable();
	}

	private boolean isHazard(Material material) {
		return material == Material.LAVA
				|| material == Material.WATER
				|| material == Material.FIRE
				|| material == Material.SOUL_FIRE
				|| material == Material.CACTUS
				|| material == Material.MAGMA_BLOCK;
	}

	private boolean isNetherRoof(Location location) {
		return location.getWorld().getEnvironment() == World.Environment.NETHER
				&& location.clone().add(0, -1, 0).getBlock().getType() == Material.BEDROCK;
	}

	public void reload() {
		config = plugin.getTeleportConfig().get();
	}
}
