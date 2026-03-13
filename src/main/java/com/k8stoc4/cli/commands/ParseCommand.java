package com.k8stoc4.cli.commands;

import com.k8stoc4.controller.K8sToC4Controller;
import com.k8stoc4.controller.RenderOutputWriter;
import com.k8stoc4.controller.provider.FileInputProvider;
import com.k8stoc4.controller.writer.FileWriter;
import com.k8stoc4.controller.writer.SystemOutWriter;
import com.k8stoc4.render.C4DslRenderer;
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
        final C4DslRenderer.Output renderOutput = new K8sToC4Controller(new FileInputProvider(input), defaultNs, groupByLabel).execute();
        final RenderOutputWriter writer = output.isPresent() ? new FileWriter(output.get()) : new SystemOutWriter();
        writer.write(renderOutput);
    }
}
