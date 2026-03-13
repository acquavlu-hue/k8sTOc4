package com.k8stoc4.model;

import com.k8stoc4.presenter.PresenterUtils;
import io.fabric8.kubernetes.api.model.HasMetadata;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

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
    @Setter
    private String description = "";
    @Setter
    private String namespace;

    public C4Component(HasMetadata resource, String namespace, String name, String kind) {
        this.namespace = namespace != null ? namespace : Constants.DEFAULT_NAMESPACE;
        this.resource = resource;
        this.id = kind.toLowerCase() + "_" + PresenterUtils.sanitizeComponentId(name);
        this.name = name;
        this.kind = kind;
    }
}
