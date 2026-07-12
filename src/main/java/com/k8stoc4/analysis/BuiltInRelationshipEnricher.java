package com.k8stoc4.analysis;

import com.k8stoc4.visitor.C4ModelBuilderVisitor;

public final class BuiltInRelationshipEnricher implements ModelEnricher {
    @Override
    public void enrich(final AnalysisContext context, final C4ModelBuilderVisitor visitor) {
        visitor.addAllRelationships();
    }
}
