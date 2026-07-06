package com.k8stoc4.model;

import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@ToString
public  class C4Model {
    private final Map<String, C4Namespace> namespaces = new LinkedHashMap<>();
    private final Set<C4Component> clusterScopedComponents = new LinkedHashSet<>();
    private final Set<String> specifications = new LinkedHashSet<>();
    private final Set<C4Relationship> relationships = new LinkedHashSet<>();
    private final Set<ExtractedDependency> extractedDependencies = new LinkedHashSet<>();

    public C4Model(){ }

    public void addRelationship(final C4Relationship r) { relationships.add(r); }

    public void addClusterScopedComponent(final C4Component c) { clusterScopedComponents.add(c); }

    public void addExtractedDependency(final ExtractedDependency dependency) { extractedDependencies.add(dependency); }

    public Set<C4Component> getComponentsByKind(final String namespace, final String kind){
        return namespaces.get(namespace).getComponents().stream().parallel()
                .filter(c4Component ->kind.equalsIgnoreCase(c4Component.getKind()))
                .collect(Collectors.toSet());
    }

    public Set<C4Component> getClusterScopedComponentsByKind(final String kind){
        return clusterScopedComponents.stream().parallel()
                .filter(c4Component -> kind.equalsIgnoreCase(c4Component.getKind()))
                .collect(Collectors.toSet());
    }

    public Optional<C4Component> searchComponentByRef(final String ref) {
        if (ref.contains(".")) {
            final String[] splitRef = ref.split("\\.", 2);
            if (this.getNamespaces().containsKey(splitRef[0])) {
                return this.getNamespaces().get(splitRef[0]).getComponents().stream().filter(c -> c.getId().equals(splitRef[1])).findFirst();
            } else {
                return Optional.empty();
            }
        } else {
            return this.getClusterScopedComponents().stream().filter(c -> c.getId().equals(ref)).findFirst();
        }
    }
 }
