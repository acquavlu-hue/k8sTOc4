package com.k8stoc4.controller.provider;

import com.k8stoc4.controller.ResourceProvider;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileInputProvider implements ResourceProvider {

    private final String input;

    public FileInputProvider(final String input) {
        this.input = input;
    }

    @Override
    public List<HasMetadata> resources() {
        try (final KubernetesClient client = new KubernetesClientBuilder().build();
             final InputStream fis = Files.newInputStream(Paths.get(this.input))) {
            final String s = new String(fis.readAllBytes(), Charset.defaultCharset());
            return client.load(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8))).items();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Input file not found: " + input, e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading input file: " + input, e);
        }
    }
}
