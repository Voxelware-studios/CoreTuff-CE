package org.voxelware.coretuff.Utility.Diagnostic;

import java.util.List;

public interface DiagnosticCollector {

    String sectionName();

    void collect(DiagnosticDumpService service, List<String> lines);
}
