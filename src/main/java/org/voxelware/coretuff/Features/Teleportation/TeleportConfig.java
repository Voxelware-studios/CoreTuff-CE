package org.voxelware.coretuff.Features.Teleportation;

import org.bukkit.configuration.file.FileConfiguration;

public class TeleportConfig {

    private FileConfiguration config;

    public TeleportConfig() {
    }

    public void load(FileConfiguration config) {
        this.config = config;
    }

    public void reload() {
    }

    public FileConfiguration get() {
        return config;
    }
}
