package com.k8stoc4.cli;

import com.k8stoc4.cli.commands.ParseCommand;
import com.k8stoc4.cli.commands.DiscoverCommand;
import picocli.CommandLine;

@CommandLine.Command(
        name = "k8stoc4",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "CLI tool for converting Kubernetes manifests to C4 diagrams",
        subcommands = {ParseCommand.class, DiscoverCommand.class}
)
public class Main implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.err.println("Error: specify a subcommand");
        CommandLine.usage(Main.class, System.out);
    }
}
