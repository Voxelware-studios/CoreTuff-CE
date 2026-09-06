package org.voxelware.coretuff.api.diagnostics;

public interface DiagnosticProvider {

    String sectionName();

    String diagnose();
}
