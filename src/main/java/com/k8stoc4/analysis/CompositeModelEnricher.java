package com.k8stoc4.analysis;

import com.k8stoc4.visitor.C4ModelBuilderVisitor;

import java.util.List;

public final class CompositeModelEnricher implements ModelEnricher {
    private final List<ModelEnricher> enrichers;

    public CompositeModelEnricher(final List<ModelEnricher> enrichers) {
        this.enrichers = List.copyOf(enrichers);
    }

    @Override
    public void enrich(final AnalysisContext context, final C4ModelBuilderVisitor visitor) {
        for (final ModelEnricher enricher : this.enrichers) {
            enricher.enrich(context, visitor);
        }
    }
}
