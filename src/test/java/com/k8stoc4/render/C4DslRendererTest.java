package com.k8stoc4.render;

import com.k8stoc4.model.C4Model;
import com.k8stoc4.visitor.C4ModelBuilderVisitor;
import com.k8stoc4.visitor.VisitorUtils;
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

class C4DslRendererTest {
    private final ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    @SneakyThrows
    @Test
    void testRender() {
        final KubernetesClient client = new KubernetesClientBuilder().build();
        final InputStream fis = classloader.getResourceAsStream("render/input/complex.yaml");
        final List<HasMetadata> resources = client.load(fis).items();
        client.close();
        final C4ModelBuilderVisitor visitor = new C4ModelBuilderVisitor.Builder().build();

        for (HasMetadata r : resources) {
            VisitorUtils.accept(r, visitor);
        }

        visitor.addAllRelationships();

        final C4Model model = visitor.getModel();
        final C4DslRenderer renderer = new C4DslRenderer();
        final C4DslRenderer.Output output = renderer.render(model);
        final String expectedSpec = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classloader.getResourceAsStream("render/output/expected-complex-spec.txt")))).lines().collect(Collectors.joining("\n")) + "\n";
        final String expectedModel = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classloader.getResourceAsStream("render/output/expected-complex-model.txt")))).lines().collect(Collectors.joining("\n")) + "\n";
        assertEquals(expectedSpec, output.getSpec());
        assertEquals(expectedModel, output.getModel());
    }
}
