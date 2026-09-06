package org.voxelware.coretuff.internal.diagnostics;

import org.voxelware.coretuff.api.diagnostics.DiagnosticProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiagnosticProviderRegistry {

    private final Map<String, DiagnosticProvider> providers = new ConcurrentHashMap<>();

    public void register(DiagnosticProvider provider) {
        providers.put(provider.sectionName(), provider);
    }

    public void unregister(String sectionName) {
        providers.remove(sectionName);
    }

    public DiagnosticProvider get(String sectionName) {
        return providers.get(sectionName);
    }

    public List<DiagnosticProvider> getAll() {
        return List.copyOf(providers.values());
    }

    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        for (DiagnosticProvider provider : providers.values()) {
            sb.append("=== ").append(provider.sectionName()).append(" ===\n");
            String diagnose = provider.diagnose();
            if (diagnose != null) {
                sb.append(diagnose);
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }
}
