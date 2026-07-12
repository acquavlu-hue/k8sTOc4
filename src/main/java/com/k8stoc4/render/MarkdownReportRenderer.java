package com.k8stoc4.render;

import com.k8stoc4.model.C4Component;
import com.k8stoc4.model.C4Model;
import com.k8stoc4.model.C4Namespace;
import com.k8stoc4.model.C4Relationship;
import com.k8stoc4.model.ExtractedDependency;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class MarkdownReportRenderer implements ArtifactRenderer {
    public static final String REPORT_FILE = "report.md";

    @Override
    public RenderedArtifacts renderArtifacts(final C4Model model, final Set<String> kindExclusions) {
        return new RenderedArtifacts().add(REPORT_FILE, buildReport(model));
    }

    private String buildReport(final C4Model model) {
        final StringBuilder sb = new StringBuilder();
        sb.append("# Extracted Architecture Report\n\n");
        sb.append("## Components\n\n");
        sb.append("| Namespace | Kind | Name | Images |\n");
        sb.append("| --- | --- | --- | --- |\n");
        for (final C4Component component : collectComponents(model)) {
            sb.append("| ")
                .append(escape(cellNamespace(component)))
                .append(" | ")
                .append(escape(component.getKind()))
                .append(" | ")
                .append(escape(component.getName()))
                .append(" | ")
                .append(escape(joinMapValues(component.getContainerImages())))
                .append(" |\n");
        }

        sb.append("\n## Component Metadata\n\n");
        sb.append("| Component | Key | Value |\n");
        sb.append("| --- | --- | --- |\n");
        for (final C4Component component : collectComponents(model)) {
            final String componentRef = cellNamespace(component) + "." + component.getId();
            for (final Map.Entry<String, String> metadata : component.getAdditionalMetadata().entrySet()) {
                sb.append("| ")
                    .append(escape(componentRef))
                    .append(" | ")
                    .append(escape(metadata.getKey()))
                    .append(" | ")
                    .append(escape(metadata.getValue()))
                    .append(" |\n");
            }
        }

        sb.append("\n## Relationships\n\n");
        sb.append("| Source | Target | Description | Technology | Tag |\n");
        sb.append("| --- | --- | --- | --- | --- |\n");
        for (final C4Relationship relationship : collectRelationships(model)) {
            sb.append("| ")
                .append(escape(relationship.getSource()))
                .append(" | ")
                .append(escape(relationship.getTarget()))
                .append(" | ")
                .append(escape(relationship.getDescription()))
                .append(" | ")
                .append(escape(relationship.getTechnology()))
                .append(" | ")
                .append(escape(relationship.getTag()))
                .append(" |\n");
        }

        sb.append("\n## Dependencies\n\n");
        sb.append("| Source | Type | Name | Endpoint | Role | Inferred By | Details |\n");
        sb.append("| --- | --- | --- | --- | --- | --- | --- |\n");
        for (final ExtractedDependency dependency : model.getExtractedDependencies()) {
            sb.append("| ")
                .append(escape(dependency.getSource()))
                .append(" | ")
                .append(escape(dependency.getDependencyType()))
                .append(" | ")
                .append(escape(dependency.getName()))
                .append(" | ")
                .append(escape(dependency.getEndpoint()))
                .append(" | ")
                .append(escape(dependency.getRole()))
                .append(" | ")
                .append(escape(dependency.getInferredBy()))
                .append(" | ")
                .append(escape(dependency.detailsAsText()))
                .append(" |\n");
        }

        return sb.toString();
    }

    private Set<C4Component> collectComponents(final C4Model model) {
        final Set<C4Component> components = new LinkedHashSet<>(model.getClusterScopedComponents());
        for (final C4Namespace namespace : model.getNamespaces().values()) {
            components.addAll(namespace.getComponents());
        }
        return components;
    }

    private Set<C4Relationship> collectRelationships(final C4Model model) {
        final Set<C4Relationship> relationships = new LinkedHashSet<>(model.getRelationships());
        for (final C4Namespace namespace : model.getNamespaces().values()) {
            relationships.addAll(namespace.getRelationships());
        }
        return relationships;
    }

    private String cellNamespace(final C4Component component) {
        return component.getNamespace() == null ? "cluster" : component.getNamespace();
    }

    private String joinMapValues(final Map<String, String> values) {
        return values.values().stream().collect(Collectors.joining(", "));
    }

    private String escape(final String value) {
        return value == null ? "" : value.replace("|", "\\|").replace("\n", "<br/>");
    }
}
