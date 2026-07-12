package com.k8stoc4.analysis;

import com.k8stoc4.model.C4Model;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;

public final class AnalysisContext {
    private final List<HasMetadata> resources;
    private final C4Model model;

    public AnalysisContext(final List<HasMetadata> resources, final C4Model model) {
        this.resources = List.copyOf(resources);
        this.model = model;
    }

    public List<HasMetadata> getResources() {
        return this.resources;
    }

    public C4Model getModel() {
        return this.model;
    }
}
