package org.voxelware.coretuff.Utility.Diagnostic.collectors;

import org.bukkit.Bukkit;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticCollector;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;

import java.util.List;

public class PermissionCollector implements DiagnosticCollector {

    @Override
    public String sectionName() {
        return "Permission Information";
    }

    @Override
    public void collect(DiagnosticDumpService service, List<String> lines) {
        String provider = "Bukkit (default)";

        var lp = Bukkit.getPluginManager().getPlugin("LuckPerms");
        if (lp != null && lp.isEnabled()) {
            provider = "LuckPerms v" + lp.getDescription().getVersion();
        }

        lines.add("  " + pad("Permission provider", 28) + ": " + provider);

        String vaultProvider = "N/A";
        try {
            var vault = Bukkit.getPluginManager().getPlugin("Vault");
            if (vault != null && vault.isEnabled()) {
                vaultProvider = "Vault v" + vault.getDescription().getVersion();
            } else {
                vaultProvider = "Not installed";
            }
        } catch (Exception e) {
            vaultProvider = "Unavailable";
        }
        lines.add("  " + pad("Vault provider", 28) + ": " + vaultProvider);
    }

    private String pad(String s, int len) {
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append(" ");
        return sb.toString();
    }
}
