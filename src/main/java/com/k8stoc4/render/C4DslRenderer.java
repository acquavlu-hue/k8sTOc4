package com.k8stoc4.render;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import lombok.extern.slf4j.Slf4j;
import com.k8stoc4.model.*;

import java.io.StringWriter;
import java.util.*;

@Slf4j
public class C4DslRenderer {

    private static final MustacheFactory MF = new DefaultMustacheFactory();

    // Render principale: workspace
    public String renderModel(C4Model model) {
        StringBuilder sb = new StringBuilder();
        sb.append("model").append("{\n");
        
        // Prima: renderizza componenti globali (non namespaced)
        for (C4Component component : model.getGlobalComponents()) {
            sb.append(renderGlobalComponent(component));
        }
        
        // Poi: renderizza namespace
        for (C4Namespace namespace : model.getNamespaces().values()) {
            sb.append(renderNamespace(namespace));
        }
        
        sb.append("}\n");
        return sb.toString();
    }

    private String renderNamespace(C4Namespace namespace) {
        Mustache mustache = MF.compile("templates/namespace.mustache");

        List<Map<String, Object>> labelGroups = new ArrayList<>();
        for (C4LabelGroup lg : namespace.getLabelGroups()) {
            Map<String, Object> lgModel = new HashMap<>();
            lgModel.put("name", lg.getName());
            lgModel.put("components", lg.getComponents().stream()
                .map(c -> {
                    Map<String, Object> model = new HashMap<>();
                    model.put("kind", c.getKind().toLowerCase());
                    model.put("id", c.getId().replace(".", "-"));
                    model.put("name", c.getName());
                    model.put("technology", c.getKind());
                    model.put("description", c.getDescription());
                    model.put("labels", Optional.ofNullable(c.getLabels()).orElse(Map.of()).entrySet().stream()
                            .map(e -> Map.of("key", e.getKey(), "value", e.getValue()))
                            .toList());
                    model.put("annotations", Optional.ofNullable(c.getAnnotations()).orElse(Map.of()).entrySet().stream()
                            .map(e -> Map.of("key", e.getKey(), "value", e.getValue()))
                            .toList());
                    return model;
                })
                .toList());
            labelGroups.add(lgModel);
        }

        List<Map<String, Object>> comps =
                namespace.getComponents().stream()
                        .map(c -> {
                            Map<String, Object> model = new HashMap<>();

                            model.put("kind", c.getKind().toLowerCase());
                            model.put("id", c.getId().replace(".", "-"));
                            model.put("name", c.getName());
                            model.put("technology", c.getKind());
                            model.put("description", c.getDescription());

                            model.put(
                                    "labels",
                                    Optional.ofNullable(c.getLabels()).orElse(Map.of()).entrySet().stream()
                                            .map(e -> Map.of(
                                                    "key", e.getKey(),
                                                    "value", e.getValue()
                                            ))
                                            .toList()
                            );

                            model.put(
                                    "annotations",
                                    Optional.ofNullable(c.getAnnotations()).orElse(Map.of()).entrySet().stream()
                                            .map(e -> Map.of(
                                                    "key", e.getKey(),
                                                    "value", e.getValue()
                                            ))
                                            .toList()
                            );

                            return model;
                        })
                        .toList();


        List<String> relations = new ArrayList<>();

        for (C4Relationship rel : namespace.getRelationships()) {
            relations.add(rel.getSource() + " -> " + rel.getTarget());
        }

        Map<String, Object> ctx = Map.of(
                "name", namespace.getName(),
                "labelGroups", labelGroups,
                "components", comps,
                "relations", relations
        );

        StringWriter writer = new StringWriter();
        mustache.execute(writer, ctx);
        return writer.toString();
    }

    private String renderGlobalComponent(C4Component c) {
        Map<String, Object> model = new HashMap<>();
        model.put("kind", c.getKind().toLowerCase());
        model.put("id", c.getId());
        model.put("name", c.getName());
        model.put("technology", c.getKind());
        model.put("description", c.getDescription());
        model.put("labels", Optional.ofNullable(c.getLabels()).orElse(Map.of()).entrySet().stream()
                .map(e -> Map.of("key", e.getKey(), "value", e.getValue()))
                .toList());
        model.put("annotations", Optional.ofNullable(c.getAnnotations()).orElse(Map.of()).entrySet().stream()
                .map(e -> Map.of("key", e.getKey(), "value", e.getValue()))
                .toList());

        return String.format("    %s %s '%s' {\n  technology \"%s\"\n  description \"%s\"\n  metadata {\n    labels '\n%s\n    '\n    annotations '\n%s\n    '\n  }\n}\n",
            model.get("kind"),
            model.get("id"),
            model.get("name"),
            model.get("technology"),
            model.get("description"),
            renderLabels((List<Map<String, String>>) model.get("labels")),
            renderAnnotations((List<Map<String, String>>) model.get("annotations")));
    }

    private String renderLabels(List<Map<String, String>> labels) {
        return labels.stream()
            .map(l -> String.format("            %s: %s", l.get("key"), l.get("value")))
            .collect(java.util.stream.Collectors.joining("\n"));
    }

    private String renderAnnotations(List<Map<String, String>> annotations) {
        return annotations.stream()
            .map(a -> String.format("            %s: %s", a.get("key"), a.get("value")))
            .collect(java.util.stream.Collectors.joining("\n"));
    }

    public String renderRelations(C4Model model) {
        StringBuilder sb = new StringBuilder();
        for (C4Relationship rel: model.getRelationships()){
            sb.append(rel.getSource()).append(" -> ").append(rel.getTarget()).append("\n");
        }

        return sb.toString();
    }

    public String renderSpec(C4Model model) {
        StringBuilder sb = new StringBuilder();
        sb.append("specification ").append("{").append("\n");
        for (String elementName: model.getSpecifications()){
            sb.append("element ").append(" ").append(elementName).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

}
