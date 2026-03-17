package com.k8stoc4.controller.provider;

import com.k8stoc4.KubernetesClient;
import com.k8stoc4.controller.ResourceProvider;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.util.ArrayList;
import java.util.List;

public class KubeApiServerInputProvider implements ResourceProvider {

    @Override
    public List<HasMetadata> resources() {
        final List<HasMetadata> allResources = new ArrayList<>();
        allResources.addAll(KubernetesClient.getInstance().getClient().pods().inAnyNamespace().list().getItems());
        allResources.addAll(KubernetesClient.getInstance().getClient().services().inAnyNamespace().list().getItems());
        allResources.addAll(KubernetesClient.getInstance().getClient().apps().deployments().inAnyNamespace().list().getItems());
        allResources.addAll(KubernetesClient.getInstance().getClient().apps().replicaSets().inAnyNamespace().list().getItems());
        allResources.addAll(KubernetesClient.getInstance().getClient().apps().statefulSets().inAnyNamespace().list().getItems());
        allResources.addAll(KubernetesClient.getInstance().getClient().apps().daemonSets().inAnyNamespace().list().getItems());
        allResources.addAll(KubernetesClient.getInstance().getClient().batch().v1().jobs().inAnyNamespace().list().getItems());
        allResources.addAll(KubernetesClient.getInstance().getClient().configMaps().inAnyNamespace().list().getItems());
        allResources.addAll(KubernetesClient.getInstance().getClient().nodes().list().getItems());
        allResources.addAll(KubernetesClient.getInstance().getClient().secrets().inAnyNamespace().list().getItems());
        allResources.addAll(KubernetesClient.getInstance().getClient().persistentVolumeClaims().inAnyNamespace().list().getItems());
        return allResources;
    }
}
