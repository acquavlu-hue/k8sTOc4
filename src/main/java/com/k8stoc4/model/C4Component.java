package com.k8stoc4.model;

import com.k8stoc4.presenter.PresenterUtils;
import io.fabric8.kubernetes.api.model.HasMetadata;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@EqualsAndHashCode
@Getter
@ToString
public class C4Component {
    private final String name;
    private final String id;
    private final String kind;
    private final HasMetadata resource;
    private final Map<String, String> containerImages = new LinkedHashMap<>();
    private final Map<String, String> env = new LinkedHashMap<>();
    private final Map<String, String> additionalMetadata = new LinkedHashMap<>();
    @Setter
    private String description = "";
    @Setter
    private String namespace;
    @Getter
    private Set<String> tags=new HashSet<>();

    public C4Component(final HasMetadata resource, final String namespace, final String name, final String kind) {
        this.namespace = namespace != null ? namespace : Constants.DEFAULT_NAMESPACE;
        this.resource = resource;
        this.id = kind.toLowerCase() + "_" + PresenterUtils.sanitizeComponentId(name);
        this.name = name;
        this.kind = kind;
    }

    public static C4Component missing(final String namespace, final String name, final String kind) {
        return new C4Component(null, namespace, name, kind);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final C4Component component = (C4Component) o;
        return Objects.equals(id, component.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
