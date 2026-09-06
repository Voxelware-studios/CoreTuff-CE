package org.voxelware.coretuff.Utility.Diagnostic.collectors;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticCollector;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;

import java.util.List;
import java.util.Map;

public class IntegrationCollector implements DiagnosticCollector {

    private static final Map<String, String> INTEGRATIONS = Map.ofEntries(
            Map.entry("Vault", "Vault"),
            Map.entry("PlaceholderAPI", "PlaceholderAPI"),
            Map.entry("ProtocolLib", "ProtocolLib"),
            Map.entry("PacketEvents", "packetevents"),
            Map.entry("LuckPerms", "LuckPerms"),
            Map.entry("Geyser", "Geyser-Spigot"),
            Map.entry("Floodgate", "floodgate")
    );

    @Override
    public String sectionName() {
        return "Installed Integrations";
    }

    @Override
    public void collect(DiagnosticDumpService service, List<String> lines) {
        PluginManager pm = Bukkit.getPluginManager();
        for (Map.Entry<String, String> entry : INTEGRATIONS.entrySet()) {
            String label = entry.getKey();
            Plugin plugin = pm.getPlugin(entry.getValue());
            boolean enabled = plugin != null && plugin.isEnabled();
            lines.add("  " + pad(label, 28) + ": " + (enabled ? "Enabled" : "Disabled"));
        }
    }

    private String pad(String s, int len) {
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append(" ");
        return sb.toString();
    }
}
