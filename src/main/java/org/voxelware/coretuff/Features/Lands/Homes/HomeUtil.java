package org.voxelware.coretuff.Features.Lands.Homes;

import org.bukkit.entity.Player;
import org.voxelware.coretuff.Features.Lands.LandConfig;

public class HomeUtil {

    public static int getMaxHomes(Player player, LandConfig config) {
        String base = config.homePermissionBase();
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission(base + i)) {
                return i;
            }
        }
        return config.maxHomesDefault();
    }
}
