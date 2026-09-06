package org.voxelware.coretuff.Features.Lands;

import org.bukkit.configuration.file.FileConfiguration;

public final class LandConfig {

    private FileConfiguration config;

    public LandConfig() {
    }

    public void load(FileConfiguration config) {
        this.config = config;
    }

    public void reload() {
    }

    public FileConfiguration get() {
        return config;
    }

    public int homeTeleportDelay() {
        if (config == null) return 3;
        return config.getInt("lands.homes.teleport-delay-seconds", 3);
    }

    public int maxHomesDefault() {
        if (config == null) return 1;
        return config.getInt("lands.homes.max-homes-default", 1);
    }

    public String homePermissionBase() {
        if (config == null) return "coretuff.land.sethome.";
        return config.getString("lands.homes.max-homes-permission-base", "coretuff.land.sethome.");
    }

    public int warpTeleportDelay() {
        if (config == null) return 3;
        return config.getInt("lands.warps.teleport-delay-seconds", 3);
    }

    public double warpCost() {
        if (config == null) return 0.0;
        return config.getDouble("lands.warps.economy-cost", 0.0);
    }

    public String warpCostBypassPermission() {
        if (config == null) return "coretuff.land.warps.bypass-cost";
        return config.getString("lands.warps.economy-cost-permission-bypass", "coretuff.land.warps.bypass-cost");
    }

    public String warpPermissionBase() {
        if (config == null) return "coretuff.land.warps.setwarp.";
        return config.getString("lands.warps.max-warps-permission-base", "coretuff.land.warps.setwarp.");
    }

    public String warpSetPermission() {
        return "coretuff.land.warps.setwarp";
    }

    public String warpDeletePermission() {
        return "coretuff.land.warps.delwarp";
    }

    public String warpListPermission() {
        return "coretuff.land.warps.list";
    }

    public String warpOthersPermission() {
        return "coretuff.land.warps.warp.others";
    }

    public String message(String path, String fallback) {
        if (config == null) return fallback;
        return config.getString(path, fallback);
    }
}
