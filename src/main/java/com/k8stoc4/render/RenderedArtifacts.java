package com.k8stoc4.render;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class RenderedArtifacts {
    private final Map<String, String> artifacts = new LinkedHashMap<>();

    public RenderedArtifacts add(final String fileName, final String content) {
        this.artifacts.put(fileName, content);
        return this;
    }

    public Optional<String> get(final String fileName) {
        return Optional.ofNullable(this.artifacts.get(fileName));
    }

    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(this.artifacts);
    }
}
