package org.voxelware.coretuff.Utility.Diagnostic.collectors;

import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticCollector;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RuntimeStatsCollector implements DiagnosticCollector {

    private final CoreTuff plugin;

    public RuntimeStatsCollector(CoreTuff plugin) {
        this.plugin = plugin;
    }

    @Override
    public String sectionName() {
        return "Runtime Statistics";
    }

    @Override
    public void collect(DiagnosticDumpService service, List<String> lines) {
        Map<String, String> stats = new LinkedHashMap<>();

        try {
            Connection conn = plugin.databaseManager().getConnection();
            if (conn != null && !conn.isClosed()) {
                stats.put("Economy accounts", String.valueOf(countRows(conn, "economy")));
                stats.put("Transactions", String.valueOf(countRows(conn, "economy_transactions")));
                stats.put("Homes", String.valueOf(countRows(conn, "homes")));
                stats.put("Warps", String.valueOf(countRows(conn, "warps")));
                stats.put("Jailed players", String.valueOf(countRows(conn, "jailed_players")));
            } else {
                stats.put("Database", "Disconnected");
            }
        } catch (Exception e) {
            stats.put("Database", "Unavailable");
        }

        stats.put("Warnings", "N/A");
        stats.put("Registered placeholders", "N/A");

        for (Map.Entry<String, String> entry : stats.entrySet()) {
            lines.add("  " + pad(entry.getKey(), 28) + ": " + entry.getValue());
        }
    }

    private int countRows(Connection conn, String table) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private String pad(String s, int len) {
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append(" ");
        return sb.toString();
    }
}
