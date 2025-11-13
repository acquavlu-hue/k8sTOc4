package render;

import model.*;

public class C4DslRenderer {

    private static final String INDENT = "  ";

    // Render principale: workspace
    public String renderModel(C4Model model, String workspaceName) {
        StringBuilder sb = new StringBuilder();
        sb.append("model").append("{\n");
        for (C4Namespace namespace : model.getNamespaces().values()) {
            sb.append(renderNamespace(namespace, 1));
        }
        sb.append("}\n");
        return sb.toString();
    }

    // Render di un sistema
    private String renderNamespace(C4Namespace system, int level) {
        StringBuilder sb = new StringBuilder();
        String indent = INDENT.repeat(level);
        sb.append(indent).append("namespace ").append(system.getName()).append(" {\n");

        for (C4Component container : system.getComponents()) {
            sb.append(renderComponent(container, level + 1));
        }

        sb.append(indent).append("}\n");
        return sb.toString();
    }

    // Render di un container
    private String renderContainer(C4Container container, int level) {
        StringBuilder sb = new StringBuilder();
        String indent = INDENT.repeat(level);

        sb.append(indent).append("container ").append(container.getName()).append(" {\n");
        sb.append(indent).append(INDENT)
                .append("technology \"").append(container.getType()).append("\"\n");

        // Aggiunge metadata come description se presente
        if (!container.getMetadata().isEmpty()) {
            for (var entry : container.getMetadata().entrySet()) {
                sb.append(indent).append(INDENT)
                        .append(entry.getKey()).append(" \"").append(entry.getValue()).append("\"\n");
            }
        }

        // Aggiunge componenti
        for (C4Component comp : container.getComponents()) {
            sb.append(renderComponent(comp, level + 1));
        }

        sb.append(indent).append("}\n");
        return sb.toString();
    }

    // Render di un componente
    private String renderComponent(C4Component component, int level) {
        StringBuilder sb = new StringBuilder();
        String indent = INDENT.repeat(level);

        sb.append(indent).append("component ")
                .append(component.getName())
                .append(" {\n");

        sb.append(indent)
                .append(INDENT)
                .append("technology \"")
                .append(component.getKind())
                .append("\"\n");

        sb.append(indent)
                .append(INDENT)
                .append("description \"")
                .append(component.getKind())
                .append("\"\n");

//        if (!component.getEnv().isEmpty()) {
//            for (var entry : component.getEnv().entrySet()) {
//                sb.append(indent).append(INDENT)
//                        .append(entry.getKey()).append(" \"").append(entry.getValue()).append("\"\n");
//            }
//        }

        sb.append(indent).append("}\n");
        return sb.toString();
    }
}

