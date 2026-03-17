package com.k8stoc4.cli.commands;

import com.k8stoc4.KubernetesClient;
import com.k8stoc4.controller.K8sToC4Controller;
import com.k8stoc4.controller.RenderOutputWriter;
import com.k8stoc4.controller.provider.KubeApiServerInputProvider;
import com.k8stoc4.controller.writer.FileWriter;
import com.k8stoc4.controller.writer.SystemOutWriter;
import com.k8stoc4.render.C4DslRenderer;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import picocli.CommandLine;

import java.time.Duration;
import java.util.Optional;

@CommandLine.Command(
        name = "discover",
        description = "discover the cluster status"
)
public class DiscoverCommand implements Runnable {

    @CommandLine.Option(
            names = {"-o", "--output"},
            description = "output dir",
            required = false
    )
    private Optional<String> output;

    @CommandLine.Option(
            names = {"-g","--group-by-label"},
            description = "label key for grouping (e.g. app.kubernetes.io/name, app)",
            required = false
    )
    private Optional<String> groupByLabel;

    @CommandLine.Option(
            names = {"-r", "--refresh-interval"},
            description = "the number of seconds between reruns. If none, the discovery is performed only once.",
            required = false
    )
    private Optional<Integer> refreshInterval;

    @CommandLine.Option(
            names = {"--rewrite-missing"},
            description = "Whether to create entities for missing referenced objects.",
            defaultValue = "false",
            required = false
    )
    private boolean rewriteMissing;

    public DiscoverCommand() {}

    @Override
    public void run() {
        final K8sToC4Controller controller = new K8sToC4Controller(new KubeApiServerInputProvider(), Optional.empty(), groupByLabel, rewriteMissing);
        final RenderOutputWriter writer = output.isPresent() ? new FileWriter(output.get()) : new SystemOutWriter();

        if (refreshInterval.isPresent()) {
            while(true) {
                try {
                    final C4DslRenderer.Output renderOutput = controller.execute();
                    writer.write(renderOutput);
                    Thread.sleep(Duration.ofSeconds(refreshInterval.get()).toMillis());
                } catch (InterruptedException e) {
                    System.err.println("Sleep interrupted: " + e.getMessage());
                }
            }
        } else {
            final C4DslRenderer.Output renderOutput = controller.execute();
            writer.write(renderOutput);
        }
    }
}
