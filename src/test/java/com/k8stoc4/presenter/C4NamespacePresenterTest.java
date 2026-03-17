package com.k8stoc4.presenter;

import com.k8stoc4.KubernetesClient;
import com.k8stoc4.model.C4Component;
import com.k8stoc4.model.C4LabelGroup;
import com.k8stoc4.model.C4Namespace;
import com.k8stoc4.model.C4Relationship;
import io.fabric8.kubernetes.api.model.HasMetadata;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class C4NamespacePresenterTest {
    private final ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    @SneakyThrows
    @Test
    void testSimpleNamespace() {
        final InputStream fis = classloader.getResourceAsStream("presenter/bases/simple-component.yaml");
        final List<HasMetadata> resources = KubernetesClient.getInstance().getClient().load(fis).items();
        final C4Component component = new C4Component(resources.get(0), "default", "simple-component", "Pod");
        final C4Namespace namespace = new C4Namespace("test");
        namespace.addComponents(component);
        final String expected = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classloader.getResourceAsStream("presenter/namespace/expected-simple-namespace.txt")))).lines().collect(Collectors.joining("\n")) + "\n";
        assertEquals(expected, C4NamespacePresenter.present(namespace));
    }

    @SneakyThrows
    @Test
    void testComplexNamespace() {
        final InputStream fis = classloader.getResourceAsStream("presenter/bases/microservice.yaml");
        final HasMetadata spuriousPod = KubernetesClient.getInstance().getClient().load(classloader.getResourceAsStream("presenter/bases/simple-component.yaml")).items().get(0);
        final List<HasMetadata> resources = KubernetesClient.getInstance().getClient().load(fis).items();
        final C4Namespace namespace = new C4Namespace("complex");
        final C4Component pod = new C4Component(spuriousPod, "default", "spurious-pod", "Pod");
        final C4LabelGroup databaseLabelGroup = namespace.getOrCreateLabelGroup("layer", "database");
        final C4LabelGroup backendLabelGroup = namespace.getOrCreateLabelGroup("layer", "backend");
        final C4LabelGroup frontendLabelGroup = namespace.getOrCreateLabelGroup("layer", "frontend");
        final C4Component appConfigmap = new C4Component(resources.get(0), "backend", "app-config", "ConfigMap");
        final C4Component postgresDeployment = new C4Component(resources.get(1), "database", "postgres", "Deployment");
        final C4Component postgresService = new C4Component(resources.get(2), "database", "postgres-service", "Service");
        final C4Component backendDeployment = new C4Component(resources.get(3), "backend", "backend", "Deployment");
        final C4Component backendService = new C4Component(resources.get(4), "backend-service", "backend", "Service");
        final C4Component frontendDeployment = new C4Component(resources.get(5), "frontend", "frontend", "Deployment");
        final C4Component frontendService = new C4Component(resources.get(6), "frontend", "frontend-service", "Service");
        databaseLabelGroup.addComponents(postgresDeployment);
        databaseLabelGroup.addComponents(postgresService);
        backendLabelGroup.addComponents(appConfigmap);
        backendLabelGroup.addComponents(backendDeployment);
        backendLabelGroup.addComponents(backendService);
        frontendLabelGroup.addComponents(frontendDeployment);
        frontendLabelGroup.addComponents(frontendService);
        namespace.addComponents(pod);
        namespace.addRelationship(new C4Relationship("frontend-service", "frontend", "", ""));
        namespace.addRelationship(new C4Relationship("backend-service", "backend", "", ""));
        namespace.addRelationship(new C4Relationship("database-service", "database", "", ""));

        final String expected = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classloader.getResourceAsStream("presenter/namespace/expected-complex-namespace.txt")))).lines().collect(Collectors.joining("\n")) + "\n";
        assertEquals(expected, C4NamespacePresenter.present(namespace));
    }
}
