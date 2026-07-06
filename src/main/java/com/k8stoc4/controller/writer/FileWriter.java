package com.k8stoc4.controller.writer;

import com.k8stoc4.controller.RenderOutputWriter;
import com.k8stoc4.render.RenderedArtifacts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileWriter implements RenderOutputWriter {
    private final String outputDir;

    public FileWriter(final String outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public void write(final RenderedArtifacts output) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Paths.get(this.outputDir).toFile().mkdirs();
            for (final var artifact : output.asMap().entrySet()) {
                this.createOrOverwriteFile(Paths.get(this.outputDir, artifact.getKey()), artifact.getValue());
            }
        } catch (IOException e) {
            throw new FileWriteException("Failed to write output files", e);
        } catch (SecurityException e) {
            throw new FileWriteException("Failed to create output directory", e);
        }
    }

    private void createOrOverwriteFile(final Path path, final String content) throws IOException {
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
