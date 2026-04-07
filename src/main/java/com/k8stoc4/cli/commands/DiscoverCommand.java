package com.k8stoc4.cli.commands;

import com.k8stoc4.controller.RenderOutputWriter;
import com.k8stoc4.controller.provider.KubeApiServerInputProvider;
import com.k8stoc4.controller.writer.FileWriter;
import com.k8stoc4.controller.writer.SystemOutWriter;
import picocli.CommandLine;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(
        name = "discover",
        description = "discover the cluster status"
)
public class DiscoverCommand extends CommonCommand implements Runnable {
    @CommandLine.Option(
            names = {"-w", "--watch"},
            description = "Whether to watch Kubernetes events or run once.",
            defaultValue = "false",
            required = false
    )
    private boolean watch;

    @CommandLine.Option(
            names = {"-r", "--refresh-interval"},
            description = "The amount of seconds between refreshes.",
            defaultValue = "60",
            required = false
    )
    private long refreshInterval;

    @Override
    public void run() {
        initController(new KubeApiServerInputProvider(), Optional.empty());
        final RenderOutputWriter writer = output.isPresent() ? new FileWriter(output.get()) : new SystemOutWriter();

        this.controller.execute(writer);
        if (this.watch) {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(refreshInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                this.controller.execute(writer);
            }
        }
    }
}
