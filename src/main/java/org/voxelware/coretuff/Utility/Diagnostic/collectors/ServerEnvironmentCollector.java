package org.voxelware.coretuff.Utility.Diagnostic.collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticCollector;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.List;

public class ServerEnvironmentCollector implements DiagnosticCollector {

    @Override
    public String sectionName() {
        return "Server Environment";
    }

    @Override
    public void collect(DiagnosticDumpService service, List<String> lines) {
        Runtime rt = Runtime.getRuntime();
        long totalMem = rt.totalMemory() / (1024 * 1024);
        long freeMem = rt.freeMemory() / (1024 * 1024);
        long usedMem = totalMem - freeMem;
        long maxMem = rt.maxMemory() == Long.MAX_VALUE ? -1 : rt.maxMemory() / (1024 * 1024);

        int cores;
        try {
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            cores = os.getAvailableProcessors();
        } catch (Exception e) {
            cores = Runtime.getRuntime().availableProcessors();
        }

        append(lines, "CPU cores", cores);
        append(lines, "Allocated memory", totalMem + " MB");
        append(lines, "Used memory", usedMem + " MB");
        append(lines, "Free memory", freeMem + " MB");
        append(lines, "Max memory", maxMem > 0 ? maxMem + " MB" : "Unlimited");
        append(lines, "Online players", Bukkit.getOnlinePlayers().size());

        List<String> worlds;
        try {
            worlds = Bukkit.getWorlds().stream().map(w -> w.getName() + " (" + w.getEnvironment().name() + ")").toList();
        } catch (Exception e) {
            worlds = List.of("Unavailable");
        }
        append(lines, "Loaded worlds", String.join(", ", worlds));
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
