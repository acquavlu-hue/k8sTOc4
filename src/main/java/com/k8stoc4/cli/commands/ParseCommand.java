package com.k8stoc4.cli.commands;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import picocli.CommandLine;
import com.k8stoc4.render.C4DslRenderer;
import com.k8stoc4.visitor.C4ModelBuilderVisitor;
import com.k8stoc4.visitor.VisitorUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

@CommandLine.Command(
        name = "parse",
        description = "converte i manifest kubernetes in un diagramma likec4"
)
public class ParseCommand implements Runnable {

    @CommandLine.Option(
            names = {"-i", "--input"},
            description = "input manifest yaml",
            required = true
    )
    private String input;

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "output dir",
            required = false
    )
    private Optional<String> output;

    @Override
    public void run() {
        try (KubernetesClient client = new KubernetesClientBuilder().build();
             FileInputStream fis = new FileInputStream(new File(input))) {

            List<HasMetadata> resources = client.load(fis).items();
            C4ModelBuilderVisitor visitor = new C4ModelBuilderVisitor();
            for (HasMetadata r : resources) {
                VisitorUtils.accept(r, visitor);
            }
            visitor.addAllRelationships();
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
