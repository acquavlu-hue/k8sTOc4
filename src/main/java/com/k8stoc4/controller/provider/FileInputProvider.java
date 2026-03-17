package com.k8stoc4.controller.provider;

import com.k8stoc4.KubernetesClient;
import com.k8stoc4.controller.ResourceProvider;
import io.fabric8.kubernetes.api.model.HasMetadata;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
        try (final InputStream fis = Files.newInputStream(Paths.get(this.input))) {
            final String s = new String(fis.readAllBytes(), Charset.defaultCharset());
            return KubernetesClient.getInstance().getClient().load(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8))).items();
        } catch (FileNotFoundException e) {
            throw new ResourceReadException("Input file not found: " + input, e);
        } catch (IOException e) {
            throw new ResourceReadException("Error reading input file: " + input, e);
        }
    }
}
