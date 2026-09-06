package org.voxelware.coretuff.Utility.Diagnostic.collectors;

import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticCollector;
import org.voxelware.coretuff.Utility.Diagnostic.DiagnosticDumpService;
import org.voxelware.coretuff.Utility.Diagnostic.ExceptionRingBuffer;

import java.util.List;

public class ExceptionCollector implements DiagnosticCollector {

    private final ExceptionRingBuffer buffer;

    public ExceptionCollector(ExceptionRingBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public String sectionName() {
        return "Recent Exceptions";
    }

    @Override
    public void collect(DiagnosticDumpService service, List<String> lines) {
        List<ExceptionRingBuffer.Record> records = buffer.snapshot();
        if (records.isEmpty()) {
            lines.add("  No recent CoreTuff exceptions.");
            return;
        }

        lines.add("  Timestamp            | Type                         | Source                      | Message");
        lines.add("  " + "-".repeat(110));
        for (ExceptionRingBuffer.Record record : records) {
            lines.add("  " + record.formatted());
        }
    }
}
