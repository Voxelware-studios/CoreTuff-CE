package org.voxelware.coretuff.internal.placeholder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.voxelware.coretuff.api.placeholder.PlaceholderRegistrar;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class PlaceholderRegistrarImpl implements PlaceholderRegistrar {

    private final Map<String, PlaceholderEntry> placeholders = new ConcurrentHashMap<>();
    private final CoreTuffPlaceholderExpansion expansion;

    public PlaceholderRegistrarImpl() {
        this.expansion = new CoreTuffPlaceholderExpansion(this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            expansion.register();
        }
    }

    @Override
    public void register(String identifier, Function<Player, String> resolver) {
        register(identifier, resolver, null);
    }

    @Override
    public void register(String identifier, Function<Player, String> resolver, String description) {
        placeholders.put(identifier.toLowerCase(), new PlaceholderEntry(identifier, resolver, description));
    }

    @Override
    public void unregister(String identifier) {
        placeholders.remove(identifier.toLowerCase());
    }

    @Override
    public boolean isRegistered(String identifier) {
        return placeholders.containsKey(identifier.toLowerCase());
    }

    @Override
    public String resolve(String identifier, Player player) {
        PlaceholderEntry entry = placeholders.get(identifier.toLowerCase());
        if (entry != null) {
            return entry.resolver().apply(player);
        }
        return null;
    }

    String resolveAll(String params, Player player) {
        if (params == null || params.isBlank()) return null;
        String lower = params.toLowerCase();
        for (Map.Entry<String, PlaceholderEntry> entry : placeholders.entrySet()) {
            if (lower.startsWith(entry.getKey() + "_") || lower.equals(entry.getKey())) {
                String identifier = entry.getKey();
                PlaceholderEntry pe = entry.getValue();
                return pe.resolver().apply(player);
            }
        }
        return null;
    }

    private record PlaceholderEntry(String identifier, Function<Player, String> resolver, String description) {}
}
