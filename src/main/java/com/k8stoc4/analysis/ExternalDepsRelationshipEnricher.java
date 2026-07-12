package com.k8stoc4.analysis;

import com.k8stoc4.common.LikeC4IdNormalizer;
import com.k8stoc4.model.*;
import com.k8stoc4.visitor.C4ModelBuilderVisitor;

import java.util.Map;

public final class ExternalDepsRelationshipEnricher implements ModelEnricher {
    @Override
    public void enrich(final AnalysisContext context,
                       final C4ModelBuilderVisitor visitor) {
        for (final C4Namespace namespace : context.getModel().getNamespaces().values()) {
            for (final C4Component component : namespace.getComponents()) {
                extractDependencies(context, component);

            }
        }
    }

    private boolean isServiceRef(final String value) {
        if (value == null) {
            return false;
        }
        return (value.startsWith("http://") ||
                value.startsWith("https://") ||
                value.startsWith("amqp://") ||
                value.startsWith("mongodb://") ||
                value.startsWith("jdbc:")) && !value.contains(".svc") && !value.contains(".jsf");
    }

    private void extractDependencies(final AnalysisContext context,
                                     final C4Component component) {

        for (final Map.Entry<String, String> entry : component.getEnv().entrySet()) {
            final String key = entry.getKey();
            final String envVarValue = entry.getValue();
            if (isServiceRef(envVarValue)) {
                System.out.println(envVarValue);


            }
        }

    }
}
