package com.k8stoc4;

import io.fabric8.kubernetes.client.KubernetesClientBuilder;

public final class KubernetesClient {
    private static KubernetesClient instance = null;

    private final io.fabric8.kubernetes.client.KubernetesClient client;

    private KubernetesClient() {
        this.client = new KubernetesClientBuilder().build();
    }

    public io.fabric8.kubernetes.client.KubernetesClient getClient() {
        return this.client;
    }

    public static KubernetesClient getInstance() {
        if (instance == null) {
            instance = new KubernetesClient();
        }
        return instance;
    }
}
