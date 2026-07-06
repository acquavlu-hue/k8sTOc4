package com.k8stoc4.controller;

import com.k8stoc4.controller.provider.FileInputProvider;
import com.k8stoc4.render.C4DslRenderer;
import com.k8stoc4.render.MarkdownReportRenderer;
import com.k8stoc4.render.RenderedArtifacts;
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
        pc.execute(writer);

        assertEquals(expectedSpec, writer.output.get(C4DslRenderer.SPEC_FILE).orElseThrow());
        assertEquals(expectedModel, writer.output.get(C4DslRenderer.MODEL_FILE).orElseThrow());
        assertEquals(true, writer.output.get(MarkdownReportRenderer.REPORT_FILE).isPresent());
    }

    private static class TestWriter implements RenderOutputWriter {
        private RenderedArtifacts output = null;

        @Override
        public void write(RenderedArtifacts output) {
            this.output = output;
        }
    }
}
