package model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public  class C4Model {
    private final Map<String, C4Namespace> namespaces = new HashMap<>();

    public void addNamespace( C4Namespace s) { namespaces.put(s.getName(),s);}
    public Map<String, C4Namespace> getNamespaces() { return namespaces; }

    private List<C4Relationship> relationships = new ArrayList<>();
    public void addRelationship(C4Relationship r) { relationships.add(r); }
    public List<C4Relationship> getRelationships() { return relationships; }
}