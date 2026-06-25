package ch.supsi.documentapplication.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class Space {

    private Long id;
    private String owner;
    private String name;
    private String description;
    private MindMap mindMap;
    private List<Chunk> chunks;

    public Space(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
