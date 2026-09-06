package org.voxelware.coretuff.Utility.Diagnostic.collectors;

import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticCollector;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;

import java.util.List;

public class ConfigVersionCollector implements DiagnosticCollector {

    private final CoreTuff plugin;

    public ConfigVersionCollector(CoreTuff plugin) {
        this.plugin = plugin;
    }

    @Override
    public String sectionName() {
        return "Configuration";
    }

    @Override
    public void collect(DiagnosticDumpService service, List<String> lines) {
        var config = plugin.getConfig();
        if (config == null) {
            lines.add("  Status: Not loaded");
            return;
        }

        lines.add("  Source: config.yml");
        lines.add("  Sections:");
        for (String key : config.getKeys(false)) {
            var section = config.getConfigurationSection(key);
            int count = section != null ? section.getKeys(true).size() : 1;
            lines.add("    " + pad(key, 20) + ": " + count + " entries");
        }
    }

    private String pad(String s, int len) {
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append(" ");
        return sb.toString();
    }
}
