package com.k8stoc4.render;

import com.k8stoc4.model.C4Component;
import com.k8stoc4.model.C4LabelGroup;
import com.k8stoc4.model.C4Model;
import com.k8stoc4.model.C4Namespace;
import com.k8stoc4.model.C4Relationship;
import com.k8stoc4.model.Constants;
import com.k8stoc4.presenter.C4ComponentPresenter;
import com.k8stoc4.presenter.C4NamespacePresenter;
import com.k8stoc4.presenter.C4RelationshipPresenter;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class C4DslRenderer {
    public C4DslRenderer() {}

    public Output render(final C4Model model, final Set<String> kindExclusions) {
        return new Output(renderModel(model), renderSpec(model), renderViews(model, kindExclusions));
    }

    // Render principale: workspace
    private String renderModel(final C4Model model) {
        final StringBuilder sb = new StringBuilder();
        sb.append("model {\n");
        sb.append(renderClusterScoped(model));
        for (final C4Namespace namespace : model.getNamespaces().values()) {
            sb.append(C4NamespacePresenter.present(namespace).lines().map(it -> Constants.INDENT + it ).collect(Collectors.joining("\n"))).append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String renderClusterScoped(final C4Model model) {
        if (model.getClusterScopedComponents() == null || model.getClusterScopedComponents().isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(Constants.INDENT).append("// Cluster-scoped resources\n");
        for (final C4Component component : model.getClusterScopedComponents()) {
            sb.append(C4ComponentPresenter.present(component).lines().map(it -> Constants.INDENT + it).collect(Collectors.joining("\n"))).append("\n");
        }
        sb.append(Constants.INDENT).append("// Cross-scope relationships\n");
        sb.append(Constants.INDENT).append("// Total model relationships: ").append(model.getRelationships().size()).append("\n");
        for (final C4Relationship relationship : model.getRelationships()) {
            sb.append(C4RelationshipPresenter.present(relationship).lines().map(it -> Constants.INDENT + it).collect(Collectors.joining("\n"))).append("\n");
        }

        return sb.toString();
    }

    private String renderSpec(final C4Model model) {
        final StringBuilder sb = new StringBuilder();
        sb.append("specification {\n");
        sb.append(Constants.INDENT.repeat(1)).append("element ").append(Constants.MISSING_TYPE).append(" {\n");
        sb.append(Constants.INDENT.repeat(2)).append("style {\n");
        sb.append(Constants.INDENT.repeat(3)).append("color red\n");
        sb.append(Constants.INDENT.repeat(3)).append("icon https://icons.getbootstrap.com/assets/icons/question-square.svg\n");
        sb.append(Constants.INDENT.repeat(2)).append("}\n");
        sb.append(Constants.INDENT.repeat(1)).append("}\n");
        sb.append(Constants.INDENT.repeat(1)).append("element namespace {\n");
        sb.append(Constants.INDENT.repeat(2)).append("style {\n");
        sb.append(Constants.INDENT.repeat(3)).append("opacity 25%\n");
        sb.append(Constants.INDENT.repeat(2)).append("}\n");
        sb.append(Constants.INDENT.repeat(1)).append("}\n");
        sb.append(Constants.INDENT.repeat(1)).append("element labelgroup").append("\n");
        for (final String elementName: model.getSpecifications()) {
            if (!"namespace".equals(elementName)) {
                sb.append(Constants.INDENT.repeat(1)).append("element ").append(elementName).append("\n");
            }
        }
        sb.append(Constants.INDENT.repeat(1)).append("tag ").append(Constants.SERVICE2SERVICE_TAG).append("\n");
        sb.append(Constants.INDENT.repeat(1)).append("tag ").append(Constants.EXTERNAL_SERVICE).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String renderViews(final C4Model model, final Set<String> kindExclusions) {
        final Set<C4Component> nodes = model.getClusterScopedComponentsByKind("Node");
        final StringBuilder sb = new StringBuilder();
        sb.append("global {\n");
        sb.append(Constants.INDENT.repeat(1)).append("style external_service element.tag = #" + Constants.EXTERNAL_SERVICE + " {\n");
        sb.append(Constants.INDENT.repeat(2)).append("color indigo\n");
        sb.append(Constants.INDENT.repeat(1)).append("}\n");
        sb.append("}\n");
        sb.append("views {\n");
        sb.append(Constants.INDENT.repeat(1)).append("global style external_service\n");
        sb.append(Constants.INDENT.repeat(1)).append("view namespaces {\n");
        sb.append(Constants.INDENT.repeat(2)).append("title 'Overviews / Namespaces'\n");
        sb.append(Constants.INDENT.repeat(2)).append("include *\n");
        sb.append(Constants.INDENT.repeat(3)).append("where kind is namespace\n");
        sb.append(Constants.INDENT.repeat(1)).append("}\n");
        if (!nodes.isEmpty()) {
            sb.append(Constants.INDENT.repeat(1)).append("view nodes {\n");
            sb.append(Constants.INDENT.repeat(2)).append("title 'Overviews / Nodes'\n");
            sb.append(Constants.INDENT.repeat(2)).append("include ").append(nodes.stream().map(C4Component::getId).collect(Collectors.joining(", "))).append("\n");
            sb.append(Constants.INDENT.repeat(2)).append("include ").append(model.getNamespaces().values().stream().map(namespace -> namespace.getName() + "._").collect(Collectors.joining(", "))).append("\n");
            sb.append(Constants.INDENT.repeat(1)).append("}\n");
        }
        for (final C4Namespace namespace : model.getNamespaces().values()) {
            sb.append(Constants.INDENT.repeat(1)).append("view of ").append(namespace.getName()).append(" {\n");
            sb.append(Constants.INDENT.repeat(2)).append("title 'Namespaces / ").append(namespace.getName()).append("'\n");
            sb.append(Constants.INDENT.repeat(2)).append("include ").append(namespace.getName()).append(".**\n");
            if (!nodes.isEmpty()) {
                sb.append(Constants.INDENT.repeat(2)).append("exclude ").append(nodes.stream().map(C4Component::getId).collect(Collectors.joining(", "))).append("\n");
            }
            if (!kindExclusions.isEmpty()) {
                sb.append(Constants.INDENT.repeat(2)).append("exclude * where ").append(kindExclusions.stream().map(it -> "kind is " + it.toLowerCase(Locale.ENGLISH)).collect(Collectors.joining(" or "))).append("\n");
            }
            sb.append(Constants.INDENT.repeat(1)).append("}\n");
            if (!namespace.getLabelGroups().isEmpty()) {
                sb.append(Constants.INDENT.repeat(1)).append("view ").append(namespace.getName()).append("-labelgroups {\n");
                sb.append(Constants.INDENT.repeat(2)).append("title 'Namespaces / ").append(namespace.getName()).append(" / LabelGroups'\n");
                sb.append(Constants.INDENT.repeat(2)).append("include ").append(namespace.getName()).append(".* where kind is labelgroup\n");
                sb.append(Constants.INDENT.repeat(1)).append("}\n");
            }
            for (final C4LabelGroup labelGroup : namespace.getLabelGroups()) {
                sb.append(Constants.INDENT.repeat(1)).append("view of ").append(namespace.getName()).append(".").append(labelGroup.getName()).append(" {\n");
                sb.append(Constants.INDENT.repeat(2)).append("title 'Namespaces / ").append(namespace.getName()).append(" / LabelGroups / ").append(labelGroup.getLabelValue()).append("'\n");
                sb.append(Constants.INDENT.repeat(2)).append("include *\n");
                sb.append(Constants.INDENT.repeat(1)).append("}\n");
            }
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
