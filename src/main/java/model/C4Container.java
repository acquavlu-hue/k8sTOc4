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
public class C4Container {
    String name;
    String type; // Deployment, StatefulSet, etc.
    List<C4Component> components = new ArrayList<>();
    private Map<String, String> metadata = new HashMap<>();

    public C4Container(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public void addComponent(C4Component c) { components.add(c); }

}