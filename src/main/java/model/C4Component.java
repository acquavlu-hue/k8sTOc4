package model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class C4Component {
    String name;
    String image;
    String kind;
    String description;
    private Map<String, String> metadata = new HashMap<>();
    private Map<String, String> env = new HashMap<>();
    public C4Component(String name, String kind) {
        this.name = name;
        this.kind = kind;
    }

}