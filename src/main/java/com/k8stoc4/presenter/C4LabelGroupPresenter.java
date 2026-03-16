package com.k8stoc4.presenter;

import com.k8stoc4.model.C4Component;
import com.k8stoc4.model.C4LabelGroup;
import com.k8stoc4.model.Constants;

import java.util.stream.Collectors;

public final class C4LabelGroupPresenter {
    private C4LabelGroupPresenter() {}
    public static String present(C4LabelGroup labelGroup) {
        final StringBuilder sb = new StringBuilder();
        sb.append("labelgroup ").append(labelGroup.getName()).append(" {\n");
        for (final C4Component component : labelGroup.getComponents()) {
            sb.append(C4ComponentPresenter.present(component).lines().map(it -> Constants.INDENT + it).collect(Collectors.joining("\n"))).append('\n');
        }
        sb.append("}\n");
        return sb.toString();
    }
}
