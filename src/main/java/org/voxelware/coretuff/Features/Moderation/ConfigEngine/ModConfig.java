package org.voxelware.coretuff.Features.Moderation.ConfigEngine;

import org.bukkit.configuration.file.FileConfiguration;

public class ModConfig {

    private FileConfiguration config;

    public ModConfig() {
    }

    public void load(FileConfiguration config) {
        this.config = config;
    }

    public void reload() {
    }

    public FileConfiguration get() {
        return config;
    }

    public int jailTeleportDelay() {
        if (config == null) return 3;
        return config.getInt("moderation.jail.teleport-delay-seconds", 3);
    }

    public boolean jailReleaseOnLogout() {
        if (config == null) return true;
        return config.getBoolean("moderation.jail.release-on-logout", true);
    }

    public String jailMessage(String path, String fallback) {
        if (config == null) return fallback;
        return config.getString("moderation.jail.messages." + path, fallback);
    }
}
