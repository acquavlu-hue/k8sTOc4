package com.k8stoc4.controller;

import com.k8stoc4.controller.provider.KubeApiServerInputProvider;
import com.k8stoc4.render.C4DslRenderer;
import com.k8stoc4.visitor.C4ModelBuilderVisitor;
import com.k8stoc4.visitor.VisitorUtils;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;
import java.util.Optional;

public class ServeController {

    private final Optional<String> defaultNamespace;
    private final Optional<String> groupByLabel;
    private final ResourceProvider resourceProvider;

    public ServeController(Optional<String> defaultNamespace, Optional<String> groupByLabel) {
        this.defaultNamespace = defaultNamespace;
        this.groupByLabel = groupByLabel;
        this.resourceProvider = new KubeApiServerInputProvider();
    }

    public C4DslRenderer.Output execute() {
        final List<HasMetadata> allResources = this.resourceProvider.resources();
        final C4ModelBuilderVisitor visitor = new C4ModelBuilderVisitor(defaultNamespace);
        for (HasMetadata r : allResources) {
            VisitorUtils.accept(r, visitor);
        }
        visitor.addAllRelationships();
        groupByLabel.ifPresent(visitor::groupComponentsByLabel);
        final C4DslRenderer renderer = new C4DslRenderer();
        return renderer.render(visitor.getModel());
    }
}
