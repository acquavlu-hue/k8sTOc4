package com.k8stoc4.render;

import com.k8stoc4.model.C4Component;
import com.k8stoc4.model.C4Model;
import com.k8stoc4.model.C4Namespace;
import com.k8stoc4.model.C4Relationship;
import com.k8stoc4.model.Constants;
import com.k8stoc4.presenter.C4ComponentPresenter;
import com.k8stoc4.presenter.C4NamespacePresenter;
import com.k8stoc4.presenter.C4RelationshipPresenter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class C4DslRenderer {
    private static final String INDENT_STRING = "    ";

    public C4DslRenderer() {}

    public Output render(final C4Model model) {
        return new Output(renderModel(model), renderSpec(model), renderViews(model));
    }

    // Render principale: workspace
    private String renderModel(final C4Model model) {
        final StringBuilder sb = new StringBuilder();
        sb.append("model {\n");
        sb.append(renderClusterScoped(model));
        for (final C4Namespace namespace : model.getNamespaces().values()) {
            sb.append(C4NamespacePresenter.present(namespace).lines().map(it -> INDENT_STRING + it ).collect(Collectors.joining("\n"))).append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String renderClusterScoped(final C4Model model) {
        if (model.getClusterScopedComponents() == null || model.getClusterScopedComponents().isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("    // Cluster-scoped resources\n");
        for (final C4Component component : model.getClusterScopedComponents()) {
            sb.append(C4ComponentPresenter.present(component).lines().map(it -> INDENT_STRING + it).collect(Collectors.joining("\n"))).append("\n");
        }
        sb.append("    // Cross-scope relationships\n");
        sb.append("    // Total model relationships: ").append(model.getRelationships().size()).append("\n");
        for (final C4Relationship relationship : model.getRelationships()) {
            sb.append(C4RelationshipPresenter.present(relationship).lines().map(it -> INDENT_STRING + it).collect(Collectors.joining("\n"))).append("\n");
        }

        return sb.toString();
    }

    private String renderSpec(final C4Model model) {
        final StringBuilder sb = new StringBuilder();
        sb.append("specification {\n");
        sb.append("    element ").append(Constants.MISSING_TYPE).append(" {\n");
        sb.append("        style {\n");
        sb.append("            color red\n");
        sb.append("            icon bootstrap:question-square\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    element namespace {\n");
        sb.append("        style {\n");
        sb.append("            opacity 25%\n");
        sb.append("        }\n");
        sb.append("    }\n");
        for (final String elementName: model.getSpecifications()) {
            if (!"namespace".equals(elementName)) {
                sb.append(INDENT_STRING).append("element ").append(elementName).append("\n");
            }
        }
        sb.append(INDENT_STRING).append("tag ").append(Constants.SERVICE2SERVICE_TAG).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String renderViews(final C4Model model) {
        final Set<C4Component> nodes = model.getClusterScopedComponentsByKind("Node");
        final StringBuilder sb = new StringBuilder();
        sb.append("views {\n");
        sb.append("    view namespaces {\n");
        sb.append("        title 'Overviews / Namespaces'\n");
        sb.append("        include *\n");
        sb.append("            where kind is namespace\n");
        sb.append("    }\n");
        if (!nodes.isEmpty()) {
            sb.append("    view nodes {\n");
            sb.append("        title 'Overviews / Nodes'\n");
            sb.append("        include ").append(nodes.stream().map(C4Component::getId).collect(Collectors.joining(", "))).append("\n");
            sb.append("        include ").append(model.getNamespaces().values().stream().map(namespace -> namespace.getName() + "._").collect(Collectors.joining(", "))).append("\n");
            sb.append("    }\n");
        }
        for (final C4Namespace namespace : model.getNamespaces().values()) {
            sb.append("    view of ").append(namespace.getName()).append(" {\n");
            sb.append("        title 'Namespaces / ").append(namespace.getName()).append("'\n");
            sb.append("        include *\n");
            if (!nodes.isEmpty()) {
                sb.append("        exclude ").append(nodes.stream().map(C4Component::getId).collect(Collectors.joining(", "))).append("\n");
            }
            sb.append("    }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    public static final class Output {
        private final String model;
        private final String spec;
        private final String view;

        private Output(final String model, final String spec, final String view) {
            this.model = model;
            this.spec = spec;
            this.view = view;
        }

        public String getModel() { return this.model; }

        public String getSpec() { return this.spec; }

        public String getView() { return this.view; }
    }

}
