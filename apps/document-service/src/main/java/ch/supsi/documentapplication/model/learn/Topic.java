package ch.supsi.documentapplication.model.learn;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
@JsonClassDescription(
        "A distinct topic extracted from a document, identified by descriptive labels, a concise title, and a factual summary.")
public class Topic {

    @JsonIgnore
    private Long id;

    @ToString.Exclude
    @Setter
    @JsonIgnore
    private String sourceFileName;

    @ToString.Exclude
    @Setter
    @JsonIgnore
    private Long page;

    @ToString.Exclude
    @Setter
    @JsonIgnore
    private Integer pageNumber;

    @ToString.Exclude
    @JsonPropertyDescription(
            "A concise title accurately reflecting this topic's main subject, grounded only in the source document.")
    private String title;

    @ToString.Exclude
    @JsonPropertyDescription(
            "A factual summary of this topic, written as direct declarative statements, grounded only in the source document. Never use meta-descriptive phrasing like 'this topic discusses' or 'the document covers'.")
    private String summary;

    @JsonPropertyDescription(
            "Maximally descriptive, information-dense, and granular labels identifying this topic's exact subject matter (e.g. named entities, precise concepts, specific mechanisms). Two topics should share a label only if they cover the exact same narrow subject. Reuse an existing label only if it matches precisely; otherwise create a new one.")
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
