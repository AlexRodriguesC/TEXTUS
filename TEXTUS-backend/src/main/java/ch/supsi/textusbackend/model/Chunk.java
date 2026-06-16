package ch.supsi.textusbackend.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public record Chunk(
        Integer page,
        String title,
        String summary,
        List<String> keywords,
        List<Entity> entities,
        List<String> questions,
        String key,
        @JsonAlias("related_keys") List<String> relatedKeys) {

    public static Chunk from(Chunk chunk, List<Entity> entities) {
        return new Chunk(
                chunk.page,
                chunk.title,
                chunk.summary,
                chunk.keywords,
                entities,
                chunk.questions,
                chunk.key,
                chunk.relatedKeys);
    }
}
