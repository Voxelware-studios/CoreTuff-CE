package org.voxelware.coretuff.Utility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.voxelware.coretuff.CoreTuff;

public class ComponentUtils {
	private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder().character('&')
			.build();
	
	private ComponentUtils() {}
	
	public static void displayCountdownTitle(CoreTuff plugin, Player player, Component title) {
		
		int[] ticks = {10};
		
		Runnable task = new Runnable() {
			@Override
			public void run() {
				
				if (!player.isOnline()) return;
				
				ticks[0]--;
				
				TextComponent subtitle = Component.text("*".repeat(ticks[0]))
						.color(NamedTextColor.GRAY)
						.append(Component.text("*".repeat(10 - ticks[0]))
								.color(NamedTextColor.DARK_GRAY));
				
				player.showTitle(Title.title(title, subtitle, 0, 20, 20));
				
				if (ticks[0] <= 0) {
					return;
				}
				
				if (plugin.isFolia()) {
					player.getScheduler().runDelayed(
							plugin,
							scheduledTask -> this.run(),
							this,
							2L
					);
				} else {
					Bukkit.getScheduler().runTaskLater(plugin, this, 2L);
				}
			}
		};

// initial run
		if (plugin.isFolia()) {
			player.getScheduler().run(
					plugin,
					scheduledTask -> task.run(),
					task
			);
		} else {
			Bukkit.getScheduler().runTask(plugin, task);
		}
	}
	
	public static TextComponent translateLegacyText(String legacy) {
		return SERIALIZER.deserialize(legacy);
	}
}
