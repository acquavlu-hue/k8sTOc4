package com.k8stoc4.presenter;

import com.k8stoc4.model.C4Component;

import java.util.Map;

public class C4ComponentPresenter {
    public static String present(C4Component component) {
        StringBuilder sb = new StringBuilder();
        sb.append(component.getKind().toLowerCase()).append(" ").append(PresenterUtils.sanitizeComponentId(component.getId())).append(" '").append(component.getName()).append("' {\n");
        sb.append("    ").append("technology \"").append(component.getKind()).append("\"\n");
        sb.append("    ").append("description \"").append(component.getDescription()).append("\"\n");
        sb.append("    ").append("metadata {\n");
        sb.append("        labels '\n");
        for (Map.Entry<String, String> label : component.getResource().getMetadata().getLabels().entrySet()) {
            sb.append("            ").append(label.getKey()).append(": ").append(label.getValue()).append("\n");
        }
        sb.append("        '\n");
        sb.append("        annotations '\n");
        for (Map.Entry<String, String> annotation : component.getResource().getMetadata().getAnnotations().entrySet()) {
            sb.append("            ").append(annotation.getKey()).append(": ").append(annotation.getValue()).append("\n");
        }
        sb.append("        '\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }
}
