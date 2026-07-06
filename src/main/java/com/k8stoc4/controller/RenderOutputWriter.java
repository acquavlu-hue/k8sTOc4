package com.k8stoc4.controller;

import com.k8stoc4.render.RenderedArtifacts;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface RenderOutputWriter {
    void write(RenderedArtifacts output);
}
