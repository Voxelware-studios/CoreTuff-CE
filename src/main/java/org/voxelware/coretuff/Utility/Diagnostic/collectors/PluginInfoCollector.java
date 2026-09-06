package org.voxelware.coretuff.Utility.Diagnostic.collectors;

import org.bukkit.Bukkit;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticCollector;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class PluginInfoCollector implements DiagnosticCollector {

    private final CoreTuff plugin;
    private final Instant enableTime;

    public PluginInfoCollector(CoreTuff plugin, Instant enableTime) {
        this.plugin = plugin;
        this.enableTime = enableTime;
    }

    @Override
    public String sectionName() {
        return "Plugin Information";
    }

    @Override
    public void collect(DiagnosticDumpService service, List<String> lines) {
        Duration uptime = Duration.between(enableTime, Instant.now());
        String uptimeStr = formatDuration(uptime);

        lines.add("  " + pad("Plugin enable time", 28) + ": " + enableTime.toString().replace("T", " ").substring(0, 19));
        lines.add("  " + pad("Plugin uptime", 28) + ": " + uptimeStr);
        lines.add("  " + pad("Version", 28) + ": " + plugin.getDescription().getVersion());
        lines.add("  " + pad("Build channel", 28) + ": " + plugin.getChannel());
        lines.add("  " + pad("API version", 28) + ": " + plugin.getDescription().getAPIVersion());
    }

    private String formatDuration(Duration d) {
        long days = d.toDays();
        long hours = d.toHours() % 24;
        long minutes = d.toMinutes() % 60;
        long seconds = d.getSeconds() % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        if (minutes > 0 || hours > 0 || days > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");
        return sb.toString();
    }

    private String pad(String s, int len) {
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append(" ");
        return sb.toString();
    }
}
