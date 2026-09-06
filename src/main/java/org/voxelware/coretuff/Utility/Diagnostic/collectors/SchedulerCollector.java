package org.voxelware.coretuff.Utility.Diagnostic.collectors;

import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticCollector;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;

import java.util.List;

public class SchedulerCollector implements DiagnosticCollector {

    private final CoreTuff plugin;

    public SchedulerCollector(CoreTuff plugin) {
        this.plugin = plugin;
    }

    @Override
    public String sectionName() {
        return "Scheduler Status";
    }

    @Override
    public void collect(DiagnosticDumpService service, List<String> lines) {
        boolean economyActive = plugin.getEconomyConfig() != null
                && plugin.getEconomyConfig().interestEnabled()
                && plugin.economyService() != null;

        String interest = economyActive ? "Running" : "Inactive";
        String interval = economyActive
                ? plugin.getEconomyConfig().interestIntervalHours() + " hours"
                : "N/A";
        String threadMode = plugin.isFolia() ? "Folia Region Scheduler" : "Main Thread";

        lines.add("  " + pad("Interest scheduler", 28) + ": " + interest);
        lines.add("  " + pad("Scheduler interval", 28) + ": " + interval);
        lines.add("  " + pad("Thread mode", 28) + ": " + threadMode);
    }

    private String pad(String s, int len) {
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append(" ");
        return sb.toString();
    }
}
