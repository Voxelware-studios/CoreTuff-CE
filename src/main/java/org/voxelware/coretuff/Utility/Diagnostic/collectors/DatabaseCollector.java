package org.voxelware.coretuff.Utility.Diagnostic.collectors;

import org.voxelware.coretuff.CoreTuff;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticCollector;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class DatabaseCollector implements DiagnosticCollector {

    private final CoreTuff plugin;

    public DatabaseCollector(CoreTuff plugin) {
        this.plugin = plugin;
    }

    @Override
    public String sectionName() {
        return "Database Information";
    }

    @Override
    public void collect(DiagnosticDumpService service, List<String> lines) {
        try {
            Connection conn = plugin.databaseManager().getConnection();
            if (conn == null || conn.isClosed()) {
                lines.add("  Connection: Closed");
                return;
            }

            String dbUrl = conn.getMetaData().getURL();
            String dbType = dbUrl != null && dbUrl.contains("h2") ? "H2" : "Unknown";
            String dbPath = dbUrl != null && dbUrl.startsWith("jdbc:h2:") ? dbUrl.substring(8) : "N/A";

            if (dbPath != null && dbPath.contains(";")) {
                dbPath = dbPath.substring(0, dbPath.indexOf(";"));
            }

            lines.add("  " + pad("Database type", 28) + ": " + dbType);
            lines.add("  " + pad("Database path", 28) + ": " + sanitizePath(dbPath));
            lines.add("  " + pad("Connection status", 28) + ": " + (conn.isValid(2) ? "Connected" : "Disconnected"));

            int tableCount = countTables(conn);
            int economyAccounts = countRows(conn, "economy");
            int homes = countRows(conn, "homes");
            int warps = countRows(conn, "warps");
            int transactions = countRows(conn, "economy_transactions");

            lines.add("  " + pad("Tables", 28) + ": " + tableCount);
            lines.add("  " + pad("Economy accounts", 28) + ": " + economyAccounts);
            lines.add("  " + pad("Homes", 28) + ": " + homes);
            lines.add("  " + pad("Warps", 28) + ": " + warps);
            lines.add("  " + pad("Transactions", 28) + ": " + transactions);

        } catch (Exception e) {
            lines.add("  " + pad("Database", 28) + ": Unavailable");
        }
    }

    private int countTables(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            return 0;
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

    private String sanitizePath(String path) {
        if (path == null) return "N/A";
        return path.replace('\\', '/');
    }

    private String pad(String s, int len) {
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append(" ");
        return sb.toString();
    }
}
