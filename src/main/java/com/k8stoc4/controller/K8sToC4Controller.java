package com.k8stoc4.controller;

import com.k8stoc4.render.C4DslRenderer;
import com.k8stoc4.visitor.C4ModelBuilderVisitor;
import com.k8stoc4.visitor.VisitorUtils;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class K8sToC4Controller {

    private final Optional<String> defaultNamespace;
    private final Optional<String> groupByLabel;
    private final ResourceProvider resourceProvider;
    private final boolean rewriteMissing;
    private final Set<String> kindExclusions;

    public K8sToC4Controller(final ResourceProvider resourceProvider, final Optional<String> defaultNamespace, final Optional<String> groupByLabel, final boolean rewriteMissing, final Set<String> kindExclusions) {
        this.defaultNamespace = defaultNamespace;
        this.groupByLabel = groupByLabel;
        this.resourceProvider = resourceProvider;
        this.rewriteMissing = rewriteMissing;
        this.kindExclusions = kindExclusions;
    }

    public void execute(final RenderOutputWriter writer) {
        final List<HasMetadata> resources = this.resourceProvider.resources();
        final C4ModelBuilderVisitor.Builder visitorBuilder = new C4ModelBuilderVisitor.Builder();
        if (this.defaultNamespace.isPresent()) {
            visitorBuilder.setDefaultNamespace(this.defaultNamespace);
        }
        final C4ModelBuilderVisitor visitor = visitorBuilder.build();
        for (final HasMetadata r : resources) {
            VisitorUtils.accept(r, visitor);
        }
        visitor.addAllRelationships();
        if (this.rewriteMissing) {
            visitor.addMissingReferencedComponents();
        }
        groupByLabel.ifPresent(visitor::groupComponentsByLabel);
        final C4DslRenderer renderer = new C4DslRenderer();
        writer.write(renderer.render(visitor.getModel(), kindExclusions));
    }
}
