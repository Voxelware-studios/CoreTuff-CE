package org.voxelware.coretuff.Utility.Diagnostic.collectors;

import org.bukkit.Bukkit;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticCollector;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;

import java.util.List;

public class PlaceholderAPICollector implements DiagnosticCollector {

    private final CoreTuff plugin;

    public PlaceholderAPICollector(CoreTuff plugin) {
        this.plugin = plugin;
    }

    @Override
    public String sectionName() {
        return "PlaceholderAPI";
    }

    @Override
    public void collect(DiagnosticDumpService service, List<String> lines) {
        var papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (papi == null || !papi.isEnabled()) {
            lines.add("  PlaceholderAPI is not installed.");
            return;
        }

        boolean expansionRegistered;
        try {
            Class<?> papiExpansion = Class.forName("me.clip.placeholderapi.expansion.PlaceholderExpansion");
            var expansion = plugin.getExpansion();
            expansionRegistered = expansion != null && papiExpansion.isInstance(expansion);
        } catch (Exception e) {
            expansionRegistered = false;
        }

        int placeholderCount;
        try {
            Class<?> papiManager = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            var players = Bukkit.getOnlinePlayers();
            if (!players.isEmpty()) {
                var result = papiManager.getMethod("getRegisteredPlaceholders");
                var placeholders = result.invoke(null);
                placeholderCount = placeholders instanceof java.util.Collection ? ((java.util.Collection<?>) placeholders).size() : 0;
            } else {
                placeholderCount = 0;
            }
        } catch (Exception e) {
            placeholderCount = 0;
        }

        lines.add("  " + pad("Expansion registered", 28) + ": " + (expansionRegistered ? "Yes" : "No"));
        lines.add("  " + pad("Registered placeholders", 28) + ": " + placeholderCount);
    }

    private String pad(String s, int len) {
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append(" ");
        return sb.toString();
    }
}
