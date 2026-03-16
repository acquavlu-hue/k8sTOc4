package com.k8stoc4.presenter;

import com.k8stoc4.model.C4Component;

import java.util.Map;

public class C4ComponentPresenter {
    public static String present(C4Component component) {
        final StringBuilder sb = new StringBuilder();
        sb.append(component.getKind().toLowerCase()).append(' ').append(PresenterUtils.sanitizeComponentId(component.getId())).append(" '").append(component.getName()).append("' {\n");
        sb.append("    technology \"").append(component.getKind()).append("\"\n");
        sb.append("    description \"").append(component.getDescription()).append("\"\n");
        sb.append("    metadata {\n");
        if (!component.getContainerImages().isEmpty()) {
            sb.append("        containerImages '\n");
            component.getContainerImages()
                    .forEach((name, image) -> sb.append("            ").append(name).append(": ").append(image).append('\n'));
            sb.append("        '\n");
        }
        if (component.getResource() != null) {
            sb.append("        labels '\n");
            for (final Map.Entry<String, String> label : component.getResource().getMetadata().getLabels().entrySet()) {
                sb.append("            ").append(label.getKey()).append(": ").append(label.getValue()).append('\n');
            }
            sb.append("        '\n");
            sb.append("        annotations '\n");
            for (final Map.Entry<String, String> annotation : component.getResource().getMetadata().getAnnotations().entrySet()) {
                sb.append("            ").append(annotation.getKey()).append(": ").append(annotation.getValue()).append('\n');
            }
            sb.append("        '\n");
        }
        component.getAdditionalMetadata().forEach((key, value) -> {
            sb.append("        ").append(key).append(" '\n");
            sb.append("            ").append(value).append('\n');
            sb.append("        '\n");
        });
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }
}
