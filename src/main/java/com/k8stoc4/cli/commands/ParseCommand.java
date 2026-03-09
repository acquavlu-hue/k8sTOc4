package com.k8stoc4.cli.commands;

import com.k8stoc4.controller.ParseController;
import picocli.CommandLine;

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
        new ParseController(input, output, defaultNs, groupByLabel).execute();
    }
}
