package model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class C4Namespace {
    private String name;
    List<C4Component> components = new ArrayList<>();
    public C4Namespace(String name) { this.name = name; }
    public void addCompoments(C4Component c) { components.add(c); }
}