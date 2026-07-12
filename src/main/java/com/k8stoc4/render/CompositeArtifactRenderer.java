package com.k8stoc4.render;

import com.k8stoc4.model.C4Model;

import java.util.List;
import java.util.Set;

public final class CompositeArtifactRenderer implements ArtifactRenderer {
    private final List<ArtifactRenderer> renderers;

    public CompositeArtifactRenderer(final List<ArtifactRenderer> renderers) {
        this.renderers = List.copyOf(renderers);
    }

    @Override
    public RenderedArtifacts renderArtifacts(final C4Model model, final Set<String> kindExclusions) {
        final RenderedArtifacts artifacts = new RenderedArtifacts();
        for (final ArtifactRenderer renderer : this.renderers) {
            renderer.renderArtifacts(model, kindExclusions).asMap().forEach(artifacts::add);
        }
        return artifacts;
    }
}
