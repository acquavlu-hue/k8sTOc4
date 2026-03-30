package com.k8stoc4.cli.commands;

import com.k8stoc4.common.KubernetesClient;
import com.k8stoc4.controller.K8sToC4Controller;
import com.k8stoc4.controller.RenderOutputWriter;
import com.k8stoc4.controller.provider.KubeApiServerInputProvider;
import com.k8stoc4.controller.writer.FileWriter;
import com.k8stoc4.controller.writer.SystemOutWriter;
import com.k8stoc4.render.C4DslRenderer;
import io.fabric8.kubernetes.api.model.events.v1.Event;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import picocli.CommandLine;

import java.util.Optional;

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

    @Override
    public void run() {
        initController(new KubeApiServerInputProvider(), Optional.empty());
        final RenderOutputWriter writer = output.isPresent() ? new FileWriter(output.get()) : new SystemOutWriter();

        final C4DslRenderer.Output renderOutput = this.controller.execute();
        writer.write(renderOutput);
        if (this.watch) {
            final EventWatcher watcher = new EventWatcher(this.controller, writer);
            while (true) {
                KubernetesClient.getInstance().getClient().events().v1().events().inAnyNamespace().watch(watcher);
            }
        }
    }

    private static final class EventWatcher implements Watcher<Event> {
        private final K8sToC4Controller controller;
        private final RenderOutputWriter writer;

        public EventWatcher(final K8sToC4Controller controller, final RenderOutputWriter writer) {
            this.controller = controller;
            this.writer = writer;
        }

        @Override
        public void eventReceived(Action action, Event resource) {
            if (action == Action.ADDED || action == Action.MODIFIED || action == Action.DELETED) {
                final C4DslRenderer.Output renderOutput = this.controller.execute();
                this.writer.write(renderOutput);
            }
        }

        @Override
        public void onClose(WatcherException cause) {
            System.err.println("Event Watcher closed: " + cause.getLocalizedMessage());
        }
    }
}
