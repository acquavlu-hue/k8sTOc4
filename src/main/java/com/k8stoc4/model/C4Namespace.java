package com.k8stoc4.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString
public class C4Namespace {
    private String name;
    private Set<C4Component> components = new HashSet<>();
    private Set<C4LabelGroup> labelGroups = new HashSet<>();
    private Set<C4Relationship> relationships = new HashSet<>();
    private Map<String, C4LabelGroup> labelGroupIndex = new HashMap<>();

    public C4Namespace(String name) { this.name = name; }
    public void addComponents(C4Component c) { components.add(c); }
    public void removeComponent(C4Component c) { components.remove(c); }
    public void addLabelGroup(C4LabelGroup lg) {
        labelGroups.add(lg);
        labelGroupIndex.put(lg.getLabelKey() + ":" + lg.getLabelValue(), lg);
    }
    public void addRelationship(C4Relationship r) { relationships.add(r); }

    public C4LabelGroup getOrCreateLabelGroup(String labelKey, String labelValue) {
        String key = labelKey + ":" + labelValue;
        C4LabelGroup group = labelGroupIndex.computeIfAbsent(key, 
            k -> new C4LabelGroup(labelKey + "_" + labelValue, labelKey, labelValue));
        labelGroups.add(group);
        return group;
    }
}
