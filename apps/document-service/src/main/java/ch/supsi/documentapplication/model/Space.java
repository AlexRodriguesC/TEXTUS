package ch.supsi.documentapplication.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
