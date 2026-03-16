package com.k8stoc4.presenter;

import com.k8stoc4.model.*;

import java.util.stream.Collectors;

public final class C4NamespacePresenter {
    private C4NamespacePresenter() {}
    public static String present(C4Namespace namespace) {
        final StringBuilder sb = new StringBuilder();
        sb.append("namespace ").append(namespace.getName()).append(" {\n");
        for (final C4LabelGroup labelGroup : namespace.getLabelGroups()) {
            sb.append(C4LabelGroupPresenter.present(labelGroup).lines().map(it -> Constants.INDENT + it).collect(Collectors.joining("\n"))).append('\n');
        }
        for (final C4Component component : namespace.getComponents()) {
            sb.append(C4ComponentPresenter.present(component).lines().map(it -> Constants.INDENT + it).collect(Collectors.joining("\n"))).append('\n');
        }
        for (final C4Relationship relationship : namespace.getRelationships()) {
            sb.append(C4RelationshipPresenter.present(relationship).lines().map(it -> Constants.INDENT + it).collect(Collectors.joining("\n"))).append('\n');
        }
        sb.append("}\n");
        return sb.toString();
    }
}
