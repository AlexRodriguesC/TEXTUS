package ch.supsi.documentapplication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Space {

    private Long id;
    private String owner;
    private String icon;
    private String name;
    private String description;

    public Space(String owner, String icon, String name, String description) {
        this.owner = owner;
        this.icon = icon;
        this.name = name;
        this.description = description;
    }
}
