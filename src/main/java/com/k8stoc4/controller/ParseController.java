package com.k8stoc4.controller;

import com.k8stoc4.controller.provider.FileInputProvider;
import com.k8stoc4.render.C4DslRenderer;
import com.k8stoc4.visitor.C4ModelBuilderVisitor;
import com.k8stoc4.visitor.VisitorUtils;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.List;
import java.util.Optional;

public class ParseController {

    private final Optional<String> defaultNamespace;
    private final Optional<String> groupByLabel;
    private final ResourceProvider resourceProvider;

    public ParseController(String input, Optional<String> defaultNamespace, Optional<String> groupByLabel) {
        this.defaultNamespace = defaultNamespace;
        this.groupByLabel = groupByLabel;
        this.resourceProvider = new FileInputProvider(input);
    }

    public C4DslRenderer.Output execute() {
        List<HasMetadata> resources = this.resourceProvider.resources();
        C4ModelBuilderVisitor visitor = new C4ModelBuilderVisitor.Builder().setDefaultNamespace(defaultNamespace).build();
        for (HasMetadata r : resources) {
            VisitorUtils.accept(r, visitor);
        }
        visitor.addAllRelationships();
        groupByLabel.ifPresent(visitor::groupComponentsByLabel);
        C4DslRenderer renderer=new C4DslRenderer();
        return renderer.render(visitor.getModel());
    }
}
