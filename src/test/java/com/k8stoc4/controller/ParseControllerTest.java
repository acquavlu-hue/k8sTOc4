package com.k8stoc4.controller;

import com.k8stoc4.render.C4DslRenderer;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseControllerTest {
    private final String input;

    public ParseControllerTest() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        this.input = classloader.getResource("controller/inputs/basic-input.yaml").getFile();
    }

    @Test
    public void testBasicInput() {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final String expectedSpec = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classloader.getResourceAsStream("controller/outputs/basic/spec.c4")))).lines().collect(Collectors.joining("\n")) + "\n";
        final String expectedModel = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classloader.getResourceAsStream("controller/outputs/basic/model.c4")))).lines().collect(Collectors.joining("\n")) + "\n";
        final ParseController pc = new ParseController(this.input, Optional.empty(), Optional.empty());
        final C4DslRenderer.Output renderOutput = pc.execute();

        assertEquals(expectedSpec, renderOutput.getSpec());
        assertEquals(expectedModel, renderOutput.getModel());
    }
}
