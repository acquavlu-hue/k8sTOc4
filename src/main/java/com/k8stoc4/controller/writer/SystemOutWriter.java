package com.k8stoc4.controller.writer;

import com.k8stoc4.controller.RenderOutputWriter;
import com.k8stoc4.render.C4DslRenderer;
import com.k8stoc4.render.RenderedArtifacts;

public class SystemOutWriter implements RenderOutputWriter {
    public SystemOutWriter() {}

    @Override
    public void write(final RenderedArtifacts output) {
        output.get(C4DslRenderer.SPEC_FILE).ifPresent(System.out::println);
        output.get(C4DslRenderer.MODEL_FILE).ifPresent(System.out::println);
        output.get(C4DslRenderer.VIEW_FILE).ifPresent(System.out::println);
        output.asMap().forEach((fileName, content) -> {
            if (!C4DslRenderer.SPEC_FILE.equals(fileName)
                && !C4DslRenderer.MODEL_FILE.equals(fileName)
                && !C4DslRenderer.VIEW_FILE.equals(fileName)) {
                System.out.println(content);
            }
        });
    }
}
