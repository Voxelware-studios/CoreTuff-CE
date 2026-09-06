package org.voxelware.coretuff.Features.Utility;

import org.bukkit.configuration.file.FileConfiguration;

public class UtilityConfig {

    private FileConfiguration config;

    public UtilityConfig() {
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
