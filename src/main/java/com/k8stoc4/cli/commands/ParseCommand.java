package com.k8stoc4.cli.commands;

import com.k8stoc4.controller.ParseController;
import com.k8stoc4.render.C4DslRenderer;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

    @CommandLine.Option(
            names = {"-n", "--namespace"},
            description = "force namespace",
            required = false
    )
    private Optional<String> defaultNs;

    @CommandLine.Option(
            names = {"-g","--group-by-label"},
            description = "label key for grouping (e.g. app.kubernetes.io/name, app)",
            required = false
    )
    private Optional<String> groupByLabel;

    @Override
    public void run() {
        final C4DslRenderer.Output renderOutput = new ParseController(input, defaultNs, groupByLabel).execute();
        if (output.isPresent()) {
            try {
                Paths.get(output.get()).toFile().mkdirs();
                Files.writeString(Paths.get(output.get(), "spec.c4"),
                        renderOutput.getSpec(), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                Files.writeString(Paths.get(output.get(), "model.c4"),
                        renderOutput.getModel(), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write output files", e);
            } catch (SecurityException e) {
                throw new RuntimeException("Failed to create output directory", e);
            }
        } else {
            System.out.println(renderOutput.getSpec());
            System.out.println(renderOutput.getModel());
        }
    }
}
