package com.k8stoc4.render;

import com.k8stoc4.presenter.C4ComponentPresenter;
import com.k8stoc4.presenter.C4NamespacePresenter;
import com.k8stoc4.presenter.C4RelationshipPresenter;
import lombok.extern.slf4j.Slf4j;
import com.k8stoc4.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class C4DslRenderer {

    public Output render(final C4Model model) {
        return new Output(renderModel(model), renderSpec(model), renderViews(model));
    }

    // Render principale: workspace
    private String renderModel(C4Model model) {
        StringBuilder sb = new StringBuilder();
        sb.append("model").append(" {\n");
        sb.append(renderClusterScoped(model));
        for (C4Namespace namespace : model.getNamespaces().values()) {
            sb.append(C4NamespacePresenter.present(namespace).lines().map(it -> "    " + it ).collect(Collectors.joining("\n"))).append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String renderClusterScoped(C4Model model) {
        if (model.getClusterScopedComponents() == null || model.getClusterScopedComponents().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("    // Cluster-scoped resources\n");
        for (final C4Component component : model.getClusterScopedComponents()) {
            sb.append(C4ComponentPresenter.present(component).lines().map(it -> "    " + it).collect(Collectors.joining("\n"))).append("\n");
        }
        sb.append("    // Cross-scope relationships\n");
        sb.append("    // Total model relationships: ").append(model.getRelationships().size()).append("\n");
        for (final C4Relationship relationship : model.getRelationships()) {
            sb.append(C4RelationshipPresenter.present(relationship).lines().map(it -> "    " + it).collect(Collectors.joining("\n"))).append("\n");
        }

        return sb.toString();
    }

    public String renderRelations(C4Model model) {
        StringBuilder sb = new StringBuilder();
        for (C4Relationship rel: model.getRelationships()){
            sb.append(rel.getSource()).append(" -> ").append(rel.getTarget()).append("\n");
        }

        return sb.toString();
    }

    private String renderSpec(C4Model model) {
        StringBuilder sb = new StringBuilder();
        sb.append("specification ").append("{").append("\n");
        for (String elementName: model.getSpecifications()) {
            sb.append("    ").append("element").append(" ").append(elementName).append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String renderViews(C4Model model) {
        final Set<C4Component> nodes = model.getClusterScopedComponentsByKind("Node");
        StringBuilder sb = new StringBuilder();
        sb.append("views {\n");
        sb.append("    view namespaces {\n");
        sb.append("        include *\n");
        sb.append("            where kind is namespace\n");
        sb.append("    }\n");
        sb.append("    view nodes {\n");
        sb.append("        include ").append(nodes.stream().map(C4Component::getId).collect(Collectors.joining(", "))).append("\n");
        sb.append("        include ").append(model.getNamespaces().values().stream().map(namespace -> namespace.getName() + "._").collect(Collectors.joining(", "))).append("\n");
        sb.append("    }\n");
        for (C4Namespace namespace : model.getNamespaces().values()) {
            sb.append("    view of ").append(namespace.getName()).append(" {\n");
            sb.append("        include *\n");
            sb.append("        exclude ").append(nodes.stream().map(n -> n.getId()).collect(Collectors.joining(", "))).append("\n");
            sb.append("    }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    public static class Output {
        private final String model;
        private final String spec;
        private final String view;

        private Output(String model, String spec, String view) {
            this.model = model;
            this.spec = spec;
            this.view = view;
        }

        public String getModel() { return this.model; }

        public String getSpec() { return this.spec; }

        public String getView() { return this.view; }
    }

}
