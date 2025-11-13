package model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class C4Relationship {
    public String source;      // nome container o component
    public String target;      // nome container o component
    public String description; // es: "HTTP request"
    public String technology;  // es: "REST API"

    public C4Relationship(String source, String target, String description, String technology) {
        this.source = source;
        this.target = target;
        this.description = description;
        this.technology = technology;
    }
}
