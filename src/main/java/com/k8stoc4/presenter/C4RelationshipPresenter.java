package com.k8stoc4.presenter;

import com.k8stoc4.model.C4Relationship;

public class C4RelationshipPresenter {
    public static String present(C4Relationship relationship) {
        StringBuilder sb = new StringBuilder();
        sb.append(PresenterUtils.sanitizeNamespacedId(relationship.getSource())).append(" -> ").append(PresenterUtils.sanitizeNamespacedId(relationship.getTarget()));
        if (!relationship.getDescription().isBlank() || !relationship.getTechnology().isBlank() || !relationship.getTag().isBlank()) {
            sb.append(" {\n");
            if (!relationship.getTag().isBlank()) {
                sb.append("    ").append(relationship.getTag()).append("\n");
            }
            if (!relationship.getDescription().isBlank()) {
                sb.append("    description '").append(relationship.getDescription()).append("'\n");
            }
            if (!relationship.getTechnology().isBlank()) {
                sb.append("    technology '").append(relationship.getTechnology()).append("'\n");
            }
            sb.append("}");
        }
        sb.append("\n");
        return sb.toString();
    }
}
