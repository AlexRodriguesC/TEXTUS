package ch.supsi.documentapplication.model;

import java.util.List;

public class Space {

    private Long id;
    private Long userId;
    private String name;
    private String description;
    private MindMap mindMap;
    private List<Chunk> chunks;
}
