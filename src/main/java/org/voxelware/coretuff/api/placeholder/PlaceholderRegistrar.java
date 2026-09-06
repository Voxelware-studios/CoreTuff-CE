package org.voxelware.coretuff.api.placeholder;

import org.bukkit.entity.Player;

import java.util.function.Function;

public interface PlaceholderRegistrar {

    void register(String identifier, Function<Player, String> resolver);

    void register(String identifier, Function<Player, String> resolver, String description);

    void unregister(String identifier);

    boolean isRegistered(String identifier);

    String resolve(String identifier, Player player);
}
