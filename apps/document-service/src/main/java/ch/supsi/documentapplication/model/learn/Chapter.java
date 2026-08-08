package ch.supsi.documentapplication.model.learn;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@JsonClassDescription(
        "One chapter in a flat, ordered list forming a coherent conceptual hierarchy, grouping a set of label ids into one self-contained unit of learning.")
public class Chapter {

    @JsonIgnore
    @ToString.Exclude
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    private final Long documentId;

    @JsonPropertyDescription(
            "Zero-based position of this chapter in the overall sequence. Must be strictly increasing by 1 across all chapters, with no gaps, reflecting learning progression from foundational to advanced.")
    private final Integer index;

    @JsonPropertyDescription("A concise chapter title, 2 to 3 words, plain text, no punctuation beyond spaces.")
    private final String title;

    @JsonPropertyDescription(
            "A concise chapter description, 6 to 7 words, plain text, summarizing the chapter's focus.")
    private final String description;

    @JsonPropertyDescription(
            "The label ids grouped into this chapter, based on semantic similarity and topic proximity. Must contain at least 3 elements, unless the total label count across all chapters is fewer than 3, and no more than 10 elements unless splitting would violate the minimum.")
    private List<Long> labels;

    @JsonIgnore
    @ToString.Exclude
    private final List<Long> topics;

    @Setter
    @JsonIgnore
    @ToString.Exclude
    private List<Lesson> lessons;

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
        this.lessons = null;
        this.documentId = null;
    }

    public Chapter(Integer index, String title, String description, List<Long> labels, List<Long> topics) {
        this(index, title, description, labels, topics, null);
    }

    public Chapter(Integer index, String title, String description, List<Long> topics, Long documentId) {
        this(index, title, description, null, topics, documentId);
    }

    public Chapter(
            Integer index, String title, String description, List<Long> labels, List<Long> topics, Long documentId) {
        this.index = index;
        this.title = title;
        this.description = description;
        this.labels = labels;
        this.topics = topics;
        this.lessons = null;
        this.documentId = documentId;
    }

    public Chapter(Long id, Integer index, String title, String description, List<Long> topics) {
        this(id, null, index, title, description, topics);
    }

    public Chapter(Long id, Long documentId, Integer index, String title, String description, List<Long> topics) {
        this.id = id;
        this.documentId = documentId;
        this.index = index;
        this.title = title;
        this.description = description;
        this.topics = topics;
    }

    /**
     * Constructs and populates the lesson list for this chapter based on its topics.
     * <p>
     * Every chapter must have at least one topic; a chapter with zero topics indicates
     * an upstream bug and results in an exception rather than an empty lesson list.
     * <p>
     * Callers must supply {@code topics} in pedagogical order (typically by ascending
     * page number). Topics are then split into contiguous lesson groups that preserve
     * that order—no interleaving—followed by a single checkpoint lesson that is not
     * associated with any topics. A chapter therefore always has at least 2 lessons:
     * one content lesson and the trailing checkpoint.
     * <p>
     * The number of content lessons is {@code floor(topicCount / 3)}, with a minimum
     * of 1. Group sizes are balanced so no group differs from another by more than one
     * topic (earlier groups absorb any remainder), and every group is guaranteed to have
     * at least 3 topics whenever {@code topicCount >= 3}. If the chapter has fewer than
     * 3 topics in total, the single content lesson simply contains all of them, since no
     * valid grouping of size 3 or more exists in that case.
     */
    public void buildLessons() {
        if (this.topics == null || this.topics.isEmpty()) {
            throw new IllegalStateException(
                    "Chapter with id " + this.id + " has no topics; this indicates a bug upstream.");
        }

        int lessonCount = computeLessonCount(this.topics.size());
        List<List<Long>> groups = distributeTopics(this.topics, lessonCount);

        List<Lesson> result = new ArrayList<>(lessonCount + 1);
        for (int i = 0; i < lessonCount; i++) {
            result.add(new Lesson(LessonType.LESSON, i, groups.get(i)));
        }
        result.add(new Lesson(LessonType.CHECKPOINT, lessonCount, new ArrayList<>()));
        this.lessons = result;
    }

    private static int computeLessonCount(int topicCount) {
        return Math.max(1, topicCount / 3);
    }

    private static List<List<Long>> distributeTopics(List<Long> topics, int lessonCount) {
        int topicCount = topics.size();
        int baseSize = topicCount / lessonCount;
        int remainder = topicCount % lessonCount;

        List<List<Long>> groups = new ArrayList<>(lessonCount);
        int offset = 0;
        for (int i = 0; i < lessonCount; i++) {
            int size = baseSize + (i < remainder ? 1 : 0);
            groups.add(new ArrayList<>(topics.subList(offset, offset + size)));
            offset += size;
        }
        return groups;
    }
}
