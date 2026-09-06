package org.voxelware.coretuff.Utility;

import org.bukkit.configuration.file.FileConfiguration;

public class CoreConfigLoader {

    private FileConfiguration config;

    public CoreConfigLoader() {
    }

    public void load(FileConfiguration config) {
        this.config = config;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void reload() {
    }

    public void save() {
    }

    public void saveConfig() {
    }

    public String getString(String path, String fallback) {
        if (config == null) return fallback;
        return config.contains(path) ? config.getString(path, fallback) : fallback;
    }

    public String getString(String path) {
        return getString(path, null);
    }

    public boolean getBoolean(String path, boolean fallback) {
        if (config == null) return fallback;
        return config.getBoolean(path, fallback);
    }

    public int getInt(String path, int fallback) {
        if (config == null) return fallback;
        return config.getInt(path, fallback);
    }
}
