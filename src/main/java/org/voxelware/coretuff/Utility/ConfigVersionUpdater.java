package org.voxelware.coretuff.Utility;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.voxelware.coretuff.CoreTuff;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public final class ConfigVersionUpdater {

    private static final DateTimeFormatter BACKUP_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

    private ConfigVersionUpdater() {}

    public static void checkAndUpdate(CoreTuff plugin) {
        Logger logger = plugin.getLogger();
        File dataFolder = plugin.getDataFolder();
        File userConfigFile = new File(dataFolder, "config.yml");

        FileConfiguration userConfig = YamlConfiguration.loadConfiguration(userConfigFile);

        FileConfiguration bundledConfig = loadBundledConfig(plugin);
        if (bundledConfig == null) {
            logger.warning("Could not load bundled config.yml from JAR. Skipping config version check.");
            return;
        }

        double userVersion = userConfig.getDouble("config-version", -1);
        double bundledVersion = bundledConfig.getDouble("config-version", -1);

        if (bundledVersion < 0) {
            logger.warning("Bundled config.yml has no config-version. Skipping config update.");
            return;
        }

        if (userVersion < 0) {
            logger.info("No config-version found in existing configuration. Migrating to latest.");
            performUpdate(plugin, userConfigFile, userConfig, bundledConfig, bundledVersion, logger);
            return;
        }

        if (Double.compare(userVersion, bundledVersion) == 0) {
            logger.info("Configuration version " + formatVersion(bundledVersion) + " is up to date.");
            return;
        }

        if (userVersion > bundledVersion) {
            logger.warning("Configuration version " + formatVersion(userVersion) + " is newer than the");
            logger.warning("running CoreTuff configuration version " + formatVersion(bundledVersion) + ".");
            logger.warning("Your configuration will NOT be overwritten.");
            return;
        }

        performUpdate(plugin, userConfigFile, userConfig, bundledConfig, bundledVersion, logger);
    }

    private static void performUpdate(CoreTuff plugin,
                                       File userConfigFile,
                                       FileConfiguration userConfig,
                                       FileConfiguration bundledConfig,
                                       double newVersion,
                                       Logger logger) {
        double oldVersion = userConfig.getDouble("config-version", 0);

        logger.info("Configuration update detected.");
        logger.info("Current configuration version: " + formatVersion(oldVersion));
        logger.info("Bundled configuration version: " + formatVersion(newVersion));

        logger.info("Backing up existing configuration...");
        File backupFile = createBackup(plugin.getDataFolder(), oldVersion, newVersion);
        if (backupFile != null) {
            logger.info("Backup saved to: " + backupFile.getPath());
        }

        logger.info("Updating configuration...");

        for (String key : userConfig.getKeys(true)) {
            if (userConfig.isConfigurationSection(key)) continue;
            bundledConfig.set(key, userConfig.get(key));
        }

        bundledConfig.set("config-version", newVersion);

        try {
            bundledConfig.save(userConfigFile);
            logger.info("Configuration successfully updated from " + formatVersion(oldVersion) + " to " + formatVersion(newVersion) + ".");
            logger.info("Existing configuration values were preserved.");
        } catch (Exception e) {
            logger.severe("Failed to save updated configuration!");
            e.printStackTrace();
        }
    }

    private static FileConfiguration loadBundledConfig(CoreTuff plugin) {
        try (InputStream is = plugin.getResource("config.yml")) {
            if (is == null) return null;
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            return YamlConfiguration.loadConfiguration(reader);
        } catch (Exception e) {
            return null;
        }
    }

    private static File createBackup(File dataFolder, double oldVersion, double newVersion) {
        File backupsDir = new File(dataFolder, "backups");
        if (!backupsDir.exists()) {
            backupsDir.mkdirs();
        }

        String timestamp = LocalDateTime.now().format(BACKUP_TS);
        String fileName = "config-" + formatVersion(oldVersion) + "-to-" + formatVersion(newVersion) + "-" + timestamp + ".yml";
        File backupFile = new File(backupsDir, fileName);

        File original = new File(dataFolder, "config.yml");
        if (!original.exists()) return null;

        try {
            FileConfiguration originalConfig = YamlConfiguration.loadConfiguration(original);
            originalConfig.save(backupFile);
            return backupFile;
        } catch (Exception e) {
            return null;
        }
    }

    private static String formatVersion(double version) {
        if (version == (long) version) {
            return String.valueOf((long) version);
        }
        return String.valueOf(version);
    }
}
