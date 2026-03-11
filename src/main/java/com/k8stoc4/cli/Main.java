package com.k8stoc4.cli;

import com.k8stoc4.cli.commands.ParseCommand;
import com.k8stoc4.cli.commands.ServeCommand;
import picocli.CommandLine;

@CommandLine.Command(
        name = "k8stoc4",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "CLI tool per convertire manifest Kubernetes in diagrammi C4",
        subcommands = {ParseCommand.class, ServeCommand.class}
)
public class Main implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.out.println("Usa un sottocomando: parse");
    }
}
