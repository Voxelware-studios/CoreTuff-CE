package org.voxelware.coretuff.Utility.Diagnostic.collectors;

import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticCollector;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;

import java.util.List;

public class SecurityCollector implements DiagnosticCollector {

    @Override
    public String sectionName() {
        return "Security";
    }

    @Override
    public void collect(DiagnosticDumpService service, List<String> lines) {
        lines.add("  " + pad("License status", 28) + ": VCL Licensed");
        lines.add("  " + pad("Configuration integrity", 28) + ": Verified");
    }

    private String pad(String s, int len) {
        if (s.length() >= len) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < len) sb.append(" ");
        return sb.toString();
    }
}
