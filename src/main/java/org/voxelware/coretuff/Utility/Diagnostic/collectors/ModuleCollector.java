package org.voxelware.coretuff.Utility.Diagnostic.collectors;

import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticCollector;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModuleCollector implements DiagnosticCollector {

    private final CoreTuff plugin;

    public ModuleCollector(CoreTuff plugin) {
        this.plugin = plugin;
    }

    @Override
    public String sectionName() {
        return "CoreTuff Modules";
    }

    @Override
    public void collect(DiagnosticDumpService service, List<String> lines) {
        Map<String, Boolean> modules = new LinkedHashMap<>();
        modules.put("Economy", plugin.economyService() != null);
        modules.put("Teleportation", true);
        modules.put("Homes", true);
        modules.put("Warps", plugin.warpService() != null);
        modules.put("Moderation", plugin.moderationService() != null);
        modules.put("Jails", plugin.jailService() != null);
        modules.put("Kits", plugin.kitService() != null);
        modules.put("Interest System", plugin.getEconomyConfig() != null && plugin.getEconomyConfig().interestEnabled());

        for (Map.Entry<String, Boolean> entry : modules.entrySet()) {
            lines.add("  " + pad(entry.getKey(), 28) + ": " + (entry.getValue() ? "Active" : "Inactive"));
        }
    }

    private String pad(String s, int len) {
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append(" ");
        return sb.toString();
    }
}
