package org.voxelware.coretuff.Features.Lands.Warps;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Features.Economy.EconomyService;
import org.voxelware.coretuff.Features.Lands.LandConfig;

import java.util.List;

public class WarpService {

    private final CoreTuff plugin;
    private final WarpRepository repository;
    private final LandConfig config;
    private final EconomyService economyService;

    public WarpService(CoreTuff plugin, WarpRepository repository, LandConfig config, EconomyService economyService) {
        this.plugin = plugin;
        this.repository = repository;
        this.config = config;
        this.economyService = economyService;
    }

    public CoreTuff plugin() {
        return plugin;
    }

    public LandConfig config() {
        return config;
    }

    public WarpRepository repository() {
        return repository;
    }

    public void setWarp(Player owner, String name, String description) throws Exception {
        Location loc = owner.getLocation();
        Warp warp = new Warp(
                name.toLowerCase(),
                loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(),
                loc.getYaw(), loc.getPitch(),
                owner.getUniqueId().toString(),
                description,
                System.currentTimeMillis()
        );
        repository.setWarp(warp);
    }

    public boolean deleteWarp(String name) throws Exception {
        return repository.deleteWarp(name);
    }

    public Warp getWarp(String name) throws Exception {
        return repository.getWarp(name);
    }

    public List<Warp> getAllWarps() throws Exception {
        return repository.getAllWarps();
    }

    public boolean warpExists(String name) throws Exception {
        return repository.exists(name);
    }

    public boolean canSetMoreWarps(Player player) throws Exception {
        int max = getMaxWarps(player);
        int current = repository.getWarpCount(player.getUniqueId().toString());
        return current < max;
    }

    public int getMaxWarps(Player player) {
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission(config.warpPermissionBase() + i)) {
                return i;
            }
        }
        return player.hasPermission(config.warpSetPermission()) ? 1 : 0;
    }

    public boolean chargeWarpCost(Player player) {
        if (economyService == null) return true;
        double cost = config.warpCost();
        if (cost <= 0) return true;
        if (player.hasPermission(config.warpCostBypassPermission())) return true;

        try {
            double balance = economyService.getBalance(player.getUniqueId());
            if (balance < cost) return false;
            economyService.withdraw(player.getUniqueId(), cost);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String warpCostFormatted() {
        if (economyService == null || config.warpCost() <= 0) return null;
        return economyService.formatter().format(config.warpCost());
    }
}
