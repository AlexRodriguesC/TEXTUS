package ch.supsi.documentapplication.model.learn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Topic {

    @JsonIgnore
    private Long id;

    @ToString.Exclude
    private String title;

    @ToString.Exclude
    private String summary;

    private List<String> labels;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return Objects.equals(getId(), topic.getId())
                && Objects.equals(getTitle(), topic.getTitle())
                && Objects.equals(getSummary(), topic.getSummary())
                && Objects.equals(getLabels(), topic.getLabels());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getSummary(), getLabels());
    }
}
