package com.k8stoc4.analysis;

import com.k8stoc4.visitor.C4ModelBuilderVisitor;

@FunctionalInterface
public interface ModelEnricher {
    void enrich(AnalysisContext context, C4ModelBuilderVisitor visitor);
}
