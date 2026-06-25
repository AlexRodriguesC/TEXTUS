package ch.supsi.documentapplication.model;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Space {

    private Long id;
    private Long userId;
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
