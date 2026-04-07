package com.k8stoc4.controller;

import com.k8stoc4.render.C4DslRenderer;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface RenderOutputWriter {
    void write(C4DslRenderer.Output output);
}
