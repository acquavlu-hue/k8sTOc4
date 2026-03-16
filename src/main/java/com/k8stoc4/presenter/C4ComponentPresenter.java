package com.k8stoc4.presenter;

import com.k8stoc4.model.C4Component;
import com.k8stoc4.model.Constants;

import java.util.Map;

public final class C4ComponentPresenter {
    private C4ComponentPresenter() {}
    public static String present(C4Component component) {
        final StringBuilder sb = new StringBuilder();
        sb.append(component.getKind().toLowerCase()).append(' ').append(PresenterUtils.sanitizeComponentId(component.getId())).append(" '").append(component.getName()).append("' {\n");
        sb.append(Constants.INDENT.repeat(1)).append("technology \"").append(component.getKind()).append("\"\n");
        sb.append(Constants.INDENT.repeat(1)).append("description \"").append(component.getDescription()).append("\"\n");
        sb.append(Constants.INDENT.repeat(1)).append("metadata {\n");
        if (!component.getContainerImages().isEmpty()) {
            sb.append(Constants.INDENT.repeat(2)).append("containerImages '\n");
            component.getContainerImages()
                    .forEach((name, image) -> sb.append(Constants.INDENT.repeat(3)).append(name).append(": ").append(image).append('\n'));
            sb.append(Constants.INDENT.repeat(2)).append("'\n");
        }
        if (component.getResource() != null) {
            sb.append(Constants.INDENT.repeat(2)).append("labels '\n");
            for (final Map.Entry<String, String> label : component.getResource().getMetadata().getLabels().entrySet()) {
                sb.append(Constants.INDENT.repeat(3)).append(label.getKey()).append(": ").append(label.getValue()).append('\n');
            }
            sb.append(Constants.INDENT.repeat(2)).append("'\n");
            sb.append(Constants.INDENT.repeat(2)).append("annotations '\n");
            for (final Map.Entry<String, String> annotation : component.getResource().getMetadata().getAnnotations().entrySet()) {
                sb.append(Constants.INDENT.repeat(3)).append(annotation.getKey()).append(": ").append(annotation.getValue()).append('\n');
            }
            sb.append(Constants.INDENT.repeat(2)).append("'\n");
        }
        component.getAdditionalMetadata().forEach((key, value) -> {
            sb.append(Constants.INDENT.repeat(2)).append(key).append(" '\n");
            sb.append(Constants.INDENT.repeat(3)).append(value).append('\n');
            sb.append(Constants.INDENT.repeat(2)).append("'\n");
        });
        sb.append(Constants.INDENT.repeat(1)).append("}\n");
        sb.append("}\n");
        return sb.toString();
    }
}
