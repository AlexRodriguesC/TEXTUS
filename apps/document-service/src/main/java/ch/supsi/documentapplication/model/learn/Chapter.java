package ch.supsi.documentapplication.model.learn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    public Chapter(
            @JsonProperty("index") Integer index,
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("labels") List<Long> labels) {
        this.index = index;
        this.title = title;
        this.description = description;
        this.labels = labels;
        this.topics = null;
    }

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
