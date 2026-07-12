package com.k8stoc4.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ExtractedDependency {
    private final String source;
    private final String dependencyType;
    private final String name;
    private final String endpoint;
    private final String role;
    private final String inferredBy;
    private final Map<String, String> details = new LinkedHashMap<>();

    public ExtractedDependency(final String source,
                               final String dependencyType,
                               final String name,
                               final String endpoint,
                               final String role,
                               final String inferredBy) {
        this.source = source;
        this.dependencyType = dependencyType;
        this.name = name;
        this.endpoint = endpoint;
        this.role = role;
        this.inferredBy = inferredBy;
    }

    public String getSource() {
        return this.source;
    }

    public String getDependencyType() {
        return this.dependencyType;
    }

    public String getName() {
        return this.name;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public String getRole() {
        return this.role;
    }

    public String getInferredBy() {
        return this.inferredBy;
    }

    public Map<String, String> getDetails() {
        return this.details;
    }

    public String detailsAsText() {
        return this.details.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExtractedDependency dependency)) {
            return false;
        }
        return Objects.equals(this.source, dependency.source)
            && Objects.equals(this.dependencyType, dependency.dependencyType)
            && Objects.equals(this.name, dependency.name)
            && Objects.equals(this.endpoint, dependency.endpoint)
            && Objects.equals(this.role, dependency.role)
            && Objects.equals(this.inferredBy, dependency.inferredBy)
            && Objects.equals(this.details, dependency.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.source, this.dependencyType, this.name, this.endpoint, this.role, this.inferredBy, this.details);
    }
}
