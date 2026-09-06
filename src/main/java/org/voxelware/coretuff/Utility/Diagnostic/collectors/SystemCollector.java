package org.voxelware.coretuff.Utility.Diagnostic.collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticCollector;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.ZoneId;
import java.util.List;

public class SystemCollector implements DiagnosticCollector {

    private final CoreTuff plugin;

    public SystemCollector(CoreTuff plugin) {
        this.plugin = plugin;
    }

    @Override
    public String sectionName() {
        return "Header";
    }

    @Override
    public void collect(DiagnosticDumpService service, List<String> lines) {
        append(lines, "CoreTuff version", plugin.getDescription().getVersion());
        append(lines, "Build channel", plugin.getChannel());
        append(lines, "Server", Bukkit.getName() + " " + Bukkit.getMinecraftVersion());
        append(lines, "Folia", plugin.isFolia() ? "Yes" : "No");
        append(lines, "Minecraft version", Bukkit.getBukkitVersion());
        append(lines, "Java version", System.getProperty("java.version"));
        append(lines, "Java vendor", System.getProperty("java.vendor"));
        append(lines, "OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        append(lines, "Architecture", System.getProperty("os.arch"));
        append(lines, "Timezone", ZoneId.systemDefault().getId());
    }

    private void append(List<String> lines, String label, Object value) {
        lines.add("  " + pad(label, 28) + ": " + (value != null ? value : "Unavailable"));
    }

    private String pad(String s, int len) {
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append(" ");
        return sb.toString();
    }
}
