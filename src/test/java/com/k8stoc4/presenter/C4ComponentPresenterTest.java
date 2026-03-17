package com.k8stoc4.presenter;

import com.k8stoc4.KubernetesClient;
import com.k8stoc4.model.C4Component;
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

class C4ComponentPresenterTest {
    private final ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    @SneakyThrows
    @Test
    void testSimpleComponent() {
        final InputStream fis = classloader.getResourceAsStream("presenter/bases/simple-component.yaml");
        final List<HasMetadata> resources = KubernetesClient.getInstance().getClient().load(fis).items();
        final C4Component component = new C4Component(resources.get(0), "default", "simple-component", "Pod");
        final String expected = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classloader.getResourceAsStream("presenter/component/expected-simple-component.txt")))).lines().collect(Collectors.joining("\n")) + "\n";
        assertEquals(expected, C4ComponentPresenter.present(component));
    }
}
