package org.voxelware.coretuff.Features.Utility.Kits;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Kit {

    private final String name;
    private final String displayName;
    private final String permission;
    private final long cooldownMillis;
    private final List<ItemStack> items;

    public Kit(String name, String displayName, String permission, long cooldownMillis, List<ItemStack> items) {
        this.name = name;
        this.displayName = displayName;
        this.permission = permission;
        this.cooldownMillis = cooldownMillis;
        this.items = items;
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getPermission() { return permission; }
    public long getCooldownMillis() { return cooldownMillis; }
    public List<ItemStack> getItems() { return items; }

    public static Kit fromConfig(String name, ConfigurationSection section) {
        String displayName = section.getString("display-name", "&f" + name);
        String permission = section.getString("permission", "coretuff.utility.kit." + name);
        long cooldownSec = section.getLong("cooldown-seconds", 0);
        long cooldownMillis = cooldownSec * 1000L;

        List<ItemStack> items = new ArrayList<>();
        var list = section.getList("items");
        if (list != null) {
            for (Object obj : list) {
                if (obj instanceof Map<?, ?> itemMap) {
                    Object matRaw = itemMap.get("material");
                    Object amtRaw = itemMap.get("amount");
                    if (matRaw == null) continue;
                    String matName = matRaw.toString();
                    int amount = (amtRaw instanceof Number n) ? n.intValue() : 1;
                    if (matName != null) {
                        Material mat = Material.getMaterial(matName.toUpperCase());
                        if (mat != null && mat.isItem()) {
                            items.add(new ItemStack(mat, amount));
                        }
                    }
                }
            }
        }
        return new Kit(name, displayName, permission, cooldownMillis, items);
    }
}
