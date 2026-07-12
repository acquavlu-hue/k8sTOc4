package com.k8stoc4.analysis;

import java.util.List;

public final class DefaultModelEnrichers {
    private DefaultModelEnrichers() {}

    public static CompositeModelEnricher create() {
        return new CompositeModelEnricher(List.of(
            new BuiltInRelationshipEnricher(),
            new KafkaTopicDependencyEnricher(),
            new ExternalDepsRelationshipEnricher()
        ));
    }
}
