package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.Getter;

@Getter
public class Chapter {

    @JsonIgnore
    private Long id;

    private final Integer index;
    private final String title;
    private final String description;
    private List<Long> labels;

    @JsonIgnore
    private final List<Long> topics;

    public Chapter(Integer index, String title, String description, List<Long> labels, List<Long> topics) {
        this.index = index;
        this.title = title;
        this.description = description;
        this.labels = labels;
        this.topics = topics;
    }

    public Chapter(Long id, Integer index, String title, String description, List<Long> topics) {
        this.id = id;
        this.index = index;
        this.title = title;
        this.description = description;
        this.topics = topics;
    }
}
