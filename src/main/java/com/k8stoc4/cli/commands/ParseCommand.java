package com.k8stoc4.cli.commands;

import com.k8stoc4.controller.RenderOutputWriter;
import com.k8stoc4.controller.provider.FileInputProvider;
import com.k8stoc4.controller.writer.FileWriter;
import com.k8stoc4.controller.writer.SystemOutWriter;
import picocli.CommandLine;

import java.util.Optional;

@CommandLine.Command(
        name = "parse",
        description = "converte i manifest kubernetes in un diagramma likec4"
)
public class ParseCommand extends CommonCommand implements Runnable {

    @CommandLine.Option(
            names = {"-i", "--input"},
            description = "input manifest yaml",
            required = true
    )
    private String input;

    @CommandLine.Option(
            names = {"-n", "--namespace"},
            description = "force namespace",
            required = false
    )
    private Optional<String> defaultNs;

    @Override
    public void run() {
        initController(new FileInputProvider(input), defaultNs);
        final RenderOutputWriter writer = output.isPresent() ? new FileWriter(output.get()) : new SystemOutWriter();
        this.controller.execute(writer);
    }
}
