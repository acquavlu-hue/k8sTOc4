package com.k8stoc4.controller.writer;

import com.k8stoc4.controller.RenderOutputWriter;
import com.k8stoc4.render.C4DslRenderer;

public class SystemOutWriter implements RenderOutputWriter {
    public SystemOutWriter() {}

    @Override
    public void write(final C4DslRenderer.Output output) {
        System.out.println(output.getSpec());
        System.out.println(output.getModel());
        System.out.println(output.getView());
    }

    @Override
    public void copyExtraResources() {}
}
