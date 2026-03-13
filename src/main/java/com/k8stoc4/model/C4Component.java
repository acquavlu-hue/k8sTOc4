package com.k8stoc4.model;

import com.k8stoc4.presenter.PresenterUtils;
import io.fabric8.kubernetes.api.model.HasMetadata;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@EqualsAndHashCode
@Getter
@Setter
@ToString
public class C4Component {

    private String namespace;
    private String name;
    private String id;
    private String image;
    private String kind;
    private String description = "";
    private Map<String, String> env = new LinkedHashMap<>();
    private HasMetadata resource;
    
    public C4Component(HasMetadata resource, String namespace, String name, String kind) {
        this.namespace = namespace != null ? namespace : Constants.DEFAULT_NAMESPACE;
        this.resource = resource;
        this.id = kind.toLowerCase() + "_" + PresenterUtils.sanitizeComponentId(name);
        this.name = name;
        this.kind = kind;
    }
}
