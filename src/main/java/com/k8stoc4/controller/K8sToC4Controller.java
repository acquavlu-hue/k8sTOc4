package com.k8stoc4.controller;

import com.k8stoc4.render.C4DslRenderer;
import com.k8stoc4.visitor.C4ModelBuilderVisitor;
import com.k8stoc4.visitor.VisitorUtils;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;
import java.util.Optional;

public final class K8sToC4Controller {

    private final Optional<String> defaultNamespace;
    private final Optional<String> groupByLabel;
    private final ResourceProvider resourceProvider;

    public K8sToC4Controller(ResourceProvider resourceProvider, Optional<String> defaultNamespace, Optional<String> groupByLabel) {
        this.defaultNamespace = defaultNamespace;
        this.groupByLabel = groupByLabel;
        this.resourceProvider = resourceProvider;
    }

    public C4DslRenderer.Output execute() {
        List<HasMetadata> resources = this.resourceProvider.resources();
        final C4ModelBuilderVisitor.Builder visitorBuilder = new C4ModelBuilderVisitor.Builder();
        if (this.defaultNamespace.isPresent()) {
            visitorBuilder.setDefaultNamespace(this.defaultNamespace);
        }
        final C4ModelBuilderVisitor visitor = visitorBuilder.build();
        for (final HasMetadata r : resources) {
            VisitorUtils.accept(r, visitor);
        }
        visitor.addAllRelationships();
        groupByLabel.ifPresent(visitor::groupComponentsByLabel);
        final C4DslRenderer renderer = new C4DslRenderer();
        return renderer.render(visitor.getModel());
    }
}
