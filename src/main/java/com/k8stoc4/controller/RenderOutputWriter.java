package com.k8stoc4.controller;

import com.k8stoc4.render.C4DslRenderer;

public interface RenderOutputWriter {
    void write(C4DslRenderer.Output output);
}
