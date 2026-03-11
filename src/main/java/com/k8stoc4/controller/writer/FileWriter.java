package com.k8stoc4.controller.writer;

import com.k8stoc4.controller.RenderOutputWriter;
import com.k8stoc4.render.C4DslRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileWriter implements RenderOutputWriter {
    private final String outputDir;
    private final static String SPEC_FILE = "spec.c4";
    private final static String MODEL_FILE = "model.c4";
    private final static String VIEW_FILE = "view.c4";

    public FileWriter(final String outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public void write(C4DslRenderer.Output output) {
        try {
            //noinspection ResultOfMethodCallIgnored
            Paths.get(this.outputDir).toFile().mkdirs();
            this.createOrOverwriteFile(Paths.get(this.outputDir, SPEC_FILE), output.getSpec());
            this.createOrOverwriteFile(Paths.get(this.outputDir, MODEL_FILE), output.getModel());
            this.createOrOverwriteFile(Paths.get(this.outputDir, VIEW_FILE), output.getView());
        } catch (IOException e) {
            throw new RuntimeException("Failed to write output files", e);
        } catch (SecurityException e) {
            throw new RuntimeException("Failed to create output directory", e);
        }
    }

    private void createOrOverwriteFile(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
