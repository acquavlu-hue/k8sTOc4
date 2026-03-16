package com.k8stoc4.presenter;

import com.k8stoc4.model.C4Relationship;
import com.k8stoc4.model.Constants;

public final class C4RelationshipPresenter {
    private C4RelationshipPresenter() {}
    public static String present(final C4Relationship relationship) {
        final StringBuilder sb = new StringBuilder();
        sb.append(PresenterUtils.sanitizeNamespacedId(relationship.getSource())).append(" -> ").append(PresenterUtils.sanitizeNamespacedId(relationship.getTarget()));
        if (!relationship.getDescription().isBlank() || !relationship.getTechnology().isBlank() || !relationship.getTag().isBlank()) {
            sb.append(" {\n");
            if (!relationship.getTag().isBlank()) {
                sb.append(Constants.INDENT.repeat(1)).append("#").append(relationship.getTag()).append('\n');
            }
            if (!relationship.getDescription().isBlank()) {
                sb.append(Constants.INDENT.repeat(1)).append("description '").append(relationship.getDescription()).append("'\n");
            }
            if (!relationship.getTechnology().isBlank()) {
                sb.append(Constants.INDENT.repeat(1)).append("technology '").append(relationship.getTechnology()).append("'\n");
            }
            sb.append('}');
        }
        sb.append('\n');
        return sb.toString();
    }
}
