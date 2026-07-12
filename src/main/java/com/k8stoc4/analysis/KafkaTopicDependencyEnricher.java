package com.k8stoc4.analysis;

import com.k8stoc4.model.C4Component;
import com.k8stoc4.model.C4Namespace;
import com.k8stoc4.model.ExtractedDependency;
import com.k8stoc4.visitor.C4ModelBuilderVisitor;

import java.util.Locale;
import java.util.Map;

public final class KafkaTopicDependencyEnricher implements ModelEnricher {
    private static final String KAFKA_PLUGIN_ID = "kafka-topic-plugin";
    private static final String KAFKA_METADATA_PREFIX = "plugin.kafka.";
    private static final String DEPENDENCY_TYPE = "kafka-topic";

    @Override
    public void enrich(final AnalysisContext context, final C4ModelBuilderVisitor visitor) {
        for (final C4Namespace namespace : context.getModel().getNamespaces().values()) {
            for (final C4Component component : namespace.getComponents()) {
                extractKafkaDependencies(context, component);
            }
        }
    }

    private void extractKafkaDependencies(final AnalysisContext context, final C4Component component) {
        for (final Map.Entry<String, String> entry : component.getEnv().entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if (isKafkaServerKey(key) && value != null && !value.isBlank()) {
                component.getAdditionalMetadata().put(KAFKA_METADATA_PREFIX + "bootstrapServers", value.trim());
            }
        }

        final String bootstrapServers = component.getAdditionalMetadata().getOrDefault(KAFKA_METADATA_PREFIX + "bootstrapServers", "");
        for (final Map.Entry<String, String> entry : component.getEnv().entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if (isKafkaTopicKey(key) && value != null && !value.isBlank()) {
                final ExtractedDependency dependency = new ExtractedDependency(
                    component.getNamespace() + "." + component.getId(),
                    DEPENDENCY_TYPE,
                    value.trim(),
                    bootstrapServers,
                    inferRole(key),
                    KAFKA_PLUGIN_ID
                );
                dependency.getDetails().put("env", key);
                context.getModel().addExtractedDependency(dependency);
            }
        }
    }

    private boolean isKafkaTopicKey(final String key) {
        final String normalized = key.toLowerCase(Locale.ENGLISH);
        return normalized.contains("kafka") && normalized.contains("topic")
            || normalized.endsWith("_topic")
            || normalized.endsWith("_topics")
            || normalized.contains("topic_name");
    }

    private boolean isKafkaServerKey(final String key) {
        final String normalized = key.toLowerCase(Locale.ENGLISH);
        return normalized.contains("kafka") && (
            normalized.contains("bootstrap")
                || normalized.contains("broker")
                || normalized.contains("server")
        );
    }

    private String inferRole(final String key) {
        final String normalized = key.toLowerCase(Locale.ENGLISH);
        if (normalized.contains("consumer")) {
            return "consumer";
        }
        if (normalized.contains("producer")) {
            return "producer";
        }
        return "unknown";
    }
}
