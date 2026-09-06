package org.voxelware.coretuff.Utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.voxelware.coretuff.CoreTuff;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.LinkedHashSet;
import java.util.Set;

public class UpdateChecker {

    private static final String API_URL =
            "https://api.modrinth.com/v2/project/coretuff-ce/version";
    private static final String DOWNLOAD_URL =
            "https://modrinth.com/plugin/coretuff-ce";

    private final CoreTuff plugin;
    private String latestFoundVersion;
    private String latestVersionType;

    public UpdateChecker(CoreTuff plugin) {
        this.plugin = plugin;
    }

    public void check() {
        new Thread(() -> {
            try {
                HttpURLConnection connection =
                        (HttpURLConnection) new URL(API_URL).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(connection.getInputStream()));
                JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
                reader.close();

                if (array.isEmpty()) return;

                Set<String> allowedTypes = getAllowedVersionTypes();
                if (allowedTypes.isEmpty()) return;

                String targetVersion = null;
                String targetType = null;

                for (var element : array) {
                    JsonObject version = element.getAsJsonObject();
                    String type = version.get("version_type").getAsString();
                    if (allowedTypes.contains(type)) {
                        targetVersion = version.get("version_number").getAsString();
                        targetType = type;
                        break;
                    }
                }

                if (targetVersion == null) return;

                String currentVersion = plugin.getPluginMeta().getVersion();

                if (!currentVersion.equalsIgnoreCase(targetVersion)) {
                    latestFoundVersion = targetVersion;
                    latestVersionType = targetType;

                    plugin.getLogger().warning("========================================");
                    plugin.getLogger().warning("New CoreTuff update available!");
                    plugin.getLogger().warning("Current Version: " + currentVersion);
                    plugin.getLogger().warning("Latest " + targetType + ": " + targetVersion);
                    plugin.getLogger().warning("Download:");
                    plugin.getLogger().warning(DOWNLOAD_URL);
                    plugin.getLogger().warning("========================================");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check for updates.");
            }
        }, "CoreTuff-UpdateChecker").start();
    }

    public void notifyPlayer(Player player) {
        if (latestFoundVersion == null || !player.isOp()) return;

        Component message = Component.text()
                .append(Component.text("[", NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(Component.text("CoreTuff", NamedTextColor.AQUA, TextDecoration.BOLD))
                .append(Component.text("]: ", NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(Component.text("New ", NamedTextColor.GRAY))
                .append(Component.text(latestVersionType, NamedTextColor.GREEN, TextDecoration.BOLD))
                .append(Component.text(" version ", NamedTextColor.GRAY))
                .append(Component.text(latestFoundVersion, NamedTextColor.YELLOW))
                .append(Component.text(" available ", NamedTextColor.GRAY))
                .append(Component.text("Download", NamedTextColor.GOLD, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.openUrl(DOWNLOAD_URL)))
                .build();

        player.sendMessage(message);
    }

    private Set<String> getAllowedVersionTypes() {
        String channel = plugin.getChannel();
        Set<String> types = new LinkedHashSet<>();

        if (channel.equalsIgnoreCase("Nightly")) {
            types.add("alpha");
            types.add("beta");
            types.add("release");
        } else if (channel.equalsIgnoreCase("Insider") || channel.equalsIgnoreCase("RC")) {
            types.add("beta");
            types.add("release");
        } else {
            types.add("release");
        }

        return types;
    }
}
