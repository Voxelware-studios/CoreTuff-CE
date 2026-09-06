package org.voxelware.coretuff.Features.Utility.Kits;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Set;

public class KitConfig {

    private FileConfiguration config;

    public KitConfig() {
    }

    public void load(FileConfiguration config) {
        this.config = config;
    }

    public void reload() {
    }

    public FileConfiguration get() {
        return config;
    }

    public int kitTeleportDelay() {
        if (config == null) return 0;
        return config.getInt("kits.kit-teleport-delay-seconds", 0);
    }

    public int defaultCooldown() {
        if (config == null) return 0;
        return config.getInt("kits.default-cooldown-seconds", 0);
    }

    public Set<String> getKitNames() {
        if (config == null) return Set.of();
        ConfigurationSection section = config.getConfigurationSection("kits.kits");
        return section == null ? Set.of() : section.getKeys(false);
    }

    public ConfigurationSection getKitSection(String name) {
        if (config == null) return null;
        return config.getConfigurationSection("kits.kits." + name);
    }
}
