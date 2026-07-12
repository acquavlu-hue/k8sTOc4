package com.k8stoc4.render;

import com.k8stoc4.model.C4Model;

import java.util.Set;

public interface ArtifactRenderer {
    RenderedArtifacts renderArtifacts(C4Model model, Set<String> kindExclusions);
}
