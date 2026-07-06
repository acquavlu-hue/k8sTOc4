package com.k8stoc4.controller;

import com.k8stoc4.analysis.AnalysisContext;
import com.k8stoc4.analysis.DefaultModelEnrichers;
import com.k8stoc4.analysis.ModelEnricher;
import com.k8stoc4.render.ArtifactRenderer;
import com.k8stoc4.render.C4DslRenderer;
import com.k8stoc4.render.CompositeArtifactRenderer;
import com.k8stoc4.render.MarkdownReportRenderer;
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
    private final ModelEnricher modelEnricher;
    private final ArtifactRenderer artifactRenderer;

    public K8sToC4Controller(final ResourceProvider resourceProvider, final Optional<String> defaultNamespace, final Optional<String> groupByLabel, final boolean rewriteMissing, final Set<String> kindExclusions) {
        this(
            resourceProvider,
            defaultNamespace,
            groupByLabel,
            rewriteMissing,
            kindExclusions,
            DefaultModelEnrichers.create(),
            new CompositeArtifactRenderer(List.of(
                new C4DslRenderer(),
                new MarkdownReportRenderer()
            ))
        );
    }

    public K8sToC4Controller(final ResourceProvider resourceProvider,
                             final Optional<String> defaultNamespace,
                             final Optional<String> groupByLabel,
                             final boolean rewriteMissing,
                             final Set<String> kindExclusions,
                             final ModelEnricher modelEnricher,
                             final ArtifactRenderer artifactRenderer) {
        this.defaultNamespace = defaultNamespace;
        this.groupByLabel = groupByLabel;
        this.resourceProvider = resourceProvider;
        this.rewriteMissing = rewriteMissing;
        this.kindExclusions = kindExclusions;
        this.modelEnricher = modelEnricher;
        this.artifactRenderer = artifactRenderer;
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
        this.modelEnricher.enrich(new AnalysisContext(resources, visitor.getModel()), visitor);
        if (this.rewriteMissing) {
            visitor.addMissingReferencedComponents();
        }
        groupByLabel.ifPresent(visitor::groupComponentsByLabel);
        writer.write(this.artifactRenderer.renderArtifacts(visitor.getModel(), kindExclusions));
    }
}
