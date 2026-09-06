package org.voxelware.coretuff.Features.Utility.miscellaneous;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.voxelware.coretuff.CoreTuff;

import org.voxelware.coretuff.Utility.CoolDown;
import org.voxelware.coretuff.commandExecuter.PlayerCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class flyCommand extends PlayerCommand {
	private final CoreTuff plugin;
	
	public flyCommand(CoreTuff plugin) {
		this.plugin = plugin;
	}
	private final Map<UUID, ScheduledTask> flyTasks = new HashMap<>();
	
	@Override
	protected List<String> generateCompletions(CommandSender sender, String label, String[] args) {
		return List.of();
	}
	
	@Override
	public boolean onCommand(@NotNull Player player, @NotNull Command command, @NotNull String label, @NonNull @NotNull String[] args) {
		FileConfiguration config = plugin.getUtilityConfig().get();
		String key = "fly";
		long cooldown = config.getLong("Fly.flycooldown")*60*1000;
		long duration = config.getLong("Fly.flyduration")*60*20;
		if(!player.hasPermission("coretuff.utility.fly")){
			player.sendMessage(plugin.format(player, plugin.getCoreString("nopermission"),null));
			return true;
		}
		boolean a = player.isOp();
		if(CoolDown.isOnCooldown(key, player, cooldown)) {
			player.sendMessage(plugin.format(player, config.getString("Fly.cooldown"),
					null));
			return true;
		}

		if(player.getAllowFlight()){
			player.sendMessage(plugin.format(player, config.getString("Fly.infly"),null));
			return true;
		}

		UUID uuid = player.getUniqueId();
		player.setAllowFlight(true);
		player.sendMessage(plugin.format(player,config.getString("Fly.flyenable"),
				Map.of(
						"time", String.valueOf(config.getLong("Fly.flyduration")))));
		CoolDown.setCooldown(key, player);
		ScheduledTask task = player.getScheduler().runDelayed(plugin, t ->{
			if(!player.isOnline()){
				return;
			}
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE,0));
			player.setAllowFlight(false);
			player.setFlying(false);
			player.sendMessage(plugin.format(player, config.getString("Fly.flyexpire"), null));
			flyTasks.remove(uuid);
			player.getScheduler().runAtFixedRate(plugin, tsk-> {
				
				if (!player.isOnline()) {
					tsk.cancel();
					return;
				}
				
				Location loc = player.getLocation();
				Block blockBelow = loc.clone().subtract(0, 0.1, 0).getBlock();
				
				boolean onGround = player.isOnGround()
						|| !blockBelow.getType().isAir()
						|| player.getVelocity().getY() == 0;
				
				if (onGround) {
					player.removePotionEffect(PotionEffectType.SLOW_FALLING);
					tsk.cancel();
				}
				
			}, null, 1L, 5L);
		},null,duration);
		flyTasks.put(uuid, task);
		return true;
	}
}
