package org.voxelware.coretuff.Features.Utility.Kits;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.voxelware.coretuff.CoreTuff;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class KitService {

    private final CoreTuff plugin;
    private final KitConfig config;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public KitService(CoreTuff plugin, KitConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public Set<String> getKitNames() {
        return config.getKitNames();
    }

    public Kit getKit(String name) {
        ConfigurationSection section = config.getKitSection(name);
        if (section == null) return null;
        return Kit.fromConfig(name, section);
    }

    public boolean canRedeem(Player player, Kit kit) {
        if (kit.getPermission() != null && !kit.getPermission().isBlank()) {
            if (!player.hasPermission(kit.getPermission())) return false;
        }
        if (kit.getCooldownMillis() <= 0) return true;
        Long lastUse = cooldowns.getOrDefault(player.getUniqueId(), Map.of()).get(kit.getName());
        if (lastUse == null) return true;
        return (System.currentTimeMillis() - lastUse) >= kit.getCooldownMillis();
    }

    public long remainingCooldown(Player player, Kit kit) {
        if (kit.getCooldownMillis() <= 0) return 0;
        Long lastUse = cooldowns.getOrDefault(player.getUniqueId(), Map.of()).get(kit.getName());
        if (lastUse == null) return 0;
        long elapsed = System.currentTimeMillis() - lastUse;
        return Math.max(0, kit.getCooldownMillis() - elapsed);
    }

    public void redeemKit(Player player, Kit kit) {
        for (ItemStack item : kit.getItems()) {
            if (item == null || item.getType() == Material.AIR) continue;
            PlayerInventory inv = player.getInventory();
            if (inv.firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), item);
            } else {
                inv.addItem(item);
            }
        }
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(kit.getName(), System.currentTimeMillis());
    }
}
