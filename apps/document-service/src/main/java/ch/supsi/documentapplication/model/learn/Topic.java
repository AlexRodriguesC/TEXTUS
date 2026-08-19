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
        "A distinct topic extracted from a document, identified by descriptive labels, a concise title, a factual summary, and the exercise shapes it supports.")
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

    @Setter
    @JsonPropertyDescription("Exercise shapes this topic genuinely supports. "
            + "Allowed values: LIST, GROUP, PAIR, QUESTION. Include every valid shape. "
            + "Use QUESTION when the content is not structurally decomposable.")
    private List<ExerciseType> exerciseTypes;

    /**
     * Plain-text source for exercise prompts: title, summary, and labels without meta headings.
     * Prefer this over {@link #toString()}, which excludes title and summary for logging.
     */
    public String toPromptSource() {
        StringBuilder builder = new StringBuilder();
        if (title != null && !title.isBlank()) {
            builder.append(title.trim());
        }
        if (summary != null && !summary.isBlank()) {
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append(summary.trim());
        }
        if (labels != null && !labels.isEmpty()) {
            List<String> cleaned = labels.stream()
                    .filter(label -> label != null && !label.isBlank())
                    .map(String::trim)
                    .toList();
            if (!cleaned.isEmpty()) {
                if (!builder.isEmpty()) {
                    builder.append('\n');
                }
                builder.append(String.join(", ", cleaned));
            }
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return Objects.equals(getId(), topic.getId())
                && Objects.equals(getTitle(), topic.getTitle())
                && Objects.equals(getSummary(), topic.getSummary())
                && Objects.equals(getLabels(), topic.getLabels())
                && Objects.equals(getExerciseTypes(), topic.getExerciseTypes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getSummary(), getLabels(), getExerciseTypes());
    }
}
