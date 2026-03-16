package com.k8stoc4.presenter;

import com.k8stoc4.model.C4Component;
import com.k8stoc4.model.C4LabelGroup;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class C4LabelGroupPresenterTest {
    private final ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    @SneakyThrows
    @Test
    void testLabelGroup() {
        try(final KubernetesClient client = new KubernetesClientBuilder().build()) {
            final InputStream fis = classloader.getResourceAsStream("presenter/bases/simple-component.yaml");
            final List<HasMetadata> resources = client.load(fis).items();
            final C4Component component = new C4Component(resources.get(0), "default", "simple-component", "Pod");
            final C4LabelGroup labelGroup = new C4LabelGroup("test-label-group", "label-key", "label-value");
            labelGroup.addComponents(component);
            final String expected = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classloader.getResourceAsStream("presenter/labelgroup/expected-label-group.txt")))).lines().collect(Collectors.joining("\n")) + "\n";
            assertEquals(expected, C4LabelGroupPresenter.present(labelGroup));
        }
    }
}
