package com.k8stoc4.controller.provider;

import com.k8stoc4.controller.ResourceProvider;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import java.util.ArrayList;
import java.util.List;

public class KubeApiServerInputProvider implements ResourceProvider {
    private final KubernetesClient client;

    public KubeApiServerInputProvider() {
        this.client = new KubernetesClientBuilder().build();
    }

    @Override
    public List<HasMetadata> resources() {
        final List<HasMetadata> allResources = new ArrayList<>();
        allResources.addAll(client.pods().inAnyNamespace().list().getItems());
        allResources.addAll(client.services().inAnyNamespace().list().getItems());
        allResources.addAll(client.apps().deployments().inAnyNamespace().list().getItems());
        allResources.addAll(client.apps().replicaSets().inAnyNamespace().list().getItems());
        allResources.addAll(client.apps().statefulSets().inAnyNamespace().list().getItems());
        allResources.addAll(client.apps().daemonSets().inAnyNamespace().list().getItems());
        allResources.addAll(client.batch().v1().jobs().inAnyNamespace().list().getItems());
        allResources.addAll(client.configMaps().inAnyNamespace().list().getItems());
        allResources.addAll(client.nodes().list().getItems());
        return allResources;
    }
}
