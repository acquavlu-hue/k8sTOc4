package com.k8stoc4.controller;

import com.k8stoc4.controller.provider.FileInputProvider;
import com.k8stoc4.render.C4DslRenderer;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class K8sToC4ControllerTest {
    private final String input;

    public K8sToC4ControllerTest() {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        this.input = Objects.requireNonNull(classloader.getResource("controller/inputs/basic-input.yaml")).getFile();
    }

    @Test
    void testBasicInput() {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final String expectedSpec = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classloader.getResourceAsStream("controller/outputs/basic/spec.c4")))).lines().collect(Collectors.joining("\n")) + "\n";
        final String expectedModel = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classloader.getResourceAsStream("controller/outputs/basic/model.c4")))).lines().collect(Collectors.joining("\n")) + "\n";
        final K8sToC4Controller pc = new K8sToC4Controller(new FileInputProvider(this.input), Optional.empty(), Optional.empty(), false, Set.of());
        final TestWriter writer = new TestWriter();
        pc.execute(writer, false);

        assertEquals(expectedSpec, writer.output.getSpec());
        assertEquals(expectedModel, writer.output.getModel());
    }

    private static class TestWriter implements RenderOutputWriter {
        private C4DslRenderer.Output output = null;

        @Override
        public void write(C4DslRenderer.Output output) {
            this.output = output;
        }

        @Override
        public void copyExtraResources() {}
    }
}
