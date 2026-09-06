package org.voxelware.coretuff.Features.Security;

import org.bukkit.configuration.file.FileConfiguration;

public final class SecurityConfig {

    private FileConfiguration config;

    public SecurityConfig() {
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
