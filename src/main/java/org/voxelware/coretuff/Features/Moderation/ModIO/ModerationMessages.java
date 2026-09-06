package org.voxelware.coretuff.Features.Moderation.ModIO;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Utility.ComponentUtils;
import org.voxelware.coretuff.Utility.CoreTuffProvider;

import java.util.List;
import java.util.Map;

public final class ModerationMessages {

	private ModerationMessages() {}

	public static String get(String path, String fallback) {
		FileConfiguration config = config();
		return config == null ? fallback : config.getString(path, fallback);
	}

	public static int getInt(String path, int fallback) {
		FileConfiguration config = config();
		return config == null ? fallback : config.getInt(path, fallback);
	}

	public static String render(String path, String fallback, Map<String, String> placeholders) {
		String message = get(path, fallback);
		return applyPlaceholders(message, placeholders);
	}

	public static List<String> getList(String path, List<String> fallback) {
		FileConfiguration config = config();
		if (config == null || !config.isList(path)) {
			return fallback;
		}

		return config.getStringList(path);
	}

	public static void send(CommandSender sender, String path, String fallback) {
		send(sender, sender instanceof Player player ? player : null, path, fallback, null);
	}

	public static void send(CommandSender sender,
	                        String path,
	                        String fallback,
	                        Map<String, String> placeholders) {
		send(sender, sender instanceof Player player ? player : null, path, fallback, placeholders);
	}

	public static void send(CommandSender sender,
	                        Player context,
	                        String path,
	                        String fallback,
	                        Map<String, String> placeholders) {
		CoreTuff plugin = plugin();
		String message = get(path, fallback);
		if (plugin == null) {
			sender.sendMessage(applyPlaceholders(message, placeholders));
			return;
		}

		sender.sendMessage(plugin.format(context, message, placeholders));
	}

	public static void sendBody(CommandSender sender,
	                            Player context,
	                            String path,
	                            String fallback,
	                            Map<String, String> placeholders) {
		CoreTuff plugin = plugin();
		String message = get(path, fallback);
		if (plugin == null) {
			sender.sendMessage(applyPlaceholders(message, placeholders));
			return;
		}

		sender.sendMessage(ComponentUtils.translateLegacyText(
				applyRuntimeFormatting(context, message, placeholders)
		));
	}

	public static void sendLines(CommandSender sender,
	                             Player context,
	                             String path,
	                             List<String> fallback,
	                             Map<String, String> placeholders) {
		CoreTuff plugin = plugin();
		for (String line : getList(path, fallback)) {
			if (plugin == null) {
				sender.sendMessage(applyPlaceholders(line, placeholders));
				continue;
			}

			sender.sendMessage(plugin.format(context, line, placeholders));
		}
	}

	public static void sendBodyLines(CommandSender sender,
	                                 Player context,
	                                 String path,
	                                 List<String> fallback,
	                                 Map<String, String> placeholders) {
		CoreTuff plugin = plugin();
		for (String line : getList(path, fallback)) {
			if (plugin == null) {
				sender.sendMessage(applyPlaceholders(line, placeholders));
				continue;
			}

			sender.sendMessage(ComponentUtils.translateLegacyText(
					applyRuntimeFormatting(context, line, placeholders)
			));
		}
	}

	public static boolean canPunish(Player sender, Player target) {
		if (sender.getUniqueId().equals(target.getUniqueId())) {
			send(sender,
					"moderation.messages.cannot-punish-self",
					"&cYou cannot punish yourself.");
			return false;
		}

		if (target.isOp()) {
			send(sender,
					"moderation.messages.cannot-punish-op",
					"&cYou cannot punish an operator.");
			return false;
		}

		return true;
	}

	private static String applyPlaceholders(String message, Map<String, String> placeholders) {
		if (message == null) {
			return "";
		}

		if (placeholders == null) {
			return message;
		}

		for (Map.Entry<String, String> entry : placeholders.entrySet()) {
			message = message.replace("{" + entry.getKey() + "}", entry.getValue());
		}
		return message;
	}

	private static FileConfiguration config() {
		CoreTuff plugin = plugin();
		return plugin == null || plugin.getModConfig() == null ? null : plugin.getModConfig().get();
	}

	private static String applyRuntimeFormatting(Player context,
	                                             String message,
	                                             Map<String, String> placeholders) {
		message = applyPlaceholders(message, placeholders);
		if (context != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			message = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(context, message);
		}
		return message;
	}

	private static CoreTuff plugin() {
		return CoreTuffProvider.getPlugin();
	}
}
