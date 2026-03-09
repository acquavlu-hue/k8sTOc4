package com.k8stoc4.controller;

import com.k8stoc4.render.C4DslRenderer;
import com.k8stoc4.visitor.C4ModelBuilderVisitor;
import com.k8stoc4.visitor.VisitorUtils;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

public class ParseController {

    private final String input;
    private final Optional<String> output;
    private final Optional<String> defaultNamespace;
    private final Optional<String> groupByLabel;

    public ParseController(String input, Optional<String> output, Optional<String> defaultNamespace, Optional<String> groupByLabel) {
        this.input = input;
        this.output = output;
        this.defaultNamespace = defaultNamespace;
        this.groupByLabel = groupByLabel;
    }

    public void execute() {
        try (KubernetesClient client = new KubernetesClientBuilder().build();
             FileInputStream fis = new FileInputStream(input)) {

            List<HasMetadata> resources = client.load(fis).items();
            C4ModelBuilderVisitor visitor = new C4ModelBuilderVisitor(defaultNamespace);
            for (HasMetadata r : resources) {
                VisitorUtils.accept(r, visitor);
            }
            visitor.addAllRelationships();
            groupByLabel.ifPresent(visitor::groupComponentsByLabel);
            C4DslRenderer renderer=new C4DslRenderer();

            if (output.isPresent()) {
                try {
                    Files.writeString(Paths.get(output.get(), "spec.c4"),
                            renderer.renderSpec(visitor.getModel()), StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                    Files.writeString(Paths.get(output.get(), "model.c4"),
                            renderer.renderModel(visitor.getModel()), StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write output files", e);
                }
            } else {
                System.out.println(renderer.renderSpec(visitor.getModel()));
                System.out.println(renderer.renderModel(visitor.getModel()));
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Input file not found: " + input, e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading input file: " + input, e);
        }
    }
}
