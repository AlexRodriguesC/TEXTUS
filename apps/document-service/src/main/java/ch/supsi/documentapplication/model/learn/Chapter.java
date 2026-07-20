package ch.supsi.documentapplication.model.learn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class Chapter {

    @JsonIgnore
    @ToString.Exclude
    private Long id;

    private final Integer index;
    private final String title;
    private final String description;

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
    }

    public Chapter(Integer index, String title, String description, List<Long> labels, List<Long> topics) {
        this.index = index;
        this.title = title;
        this.description = description;
        this.labels = labels;
        this.topics = topics;
        this.lessons = null;
    }

    public Chapter(Long id, Integer index, String title, String description, List<Long> topics) {
        this.id = id;
        this.index = index;
        this.title = title;
        this.description = description;
        this.topics = topics;
    }

    /**
     * Constructs and populates the node list for this chapter based on its topics.
     * <p>
     * Every chapter must have at least one topic; a chapter with zero topics indicates
     * an upstream bug and results in an exception rather than an empty lesson list.
     * <p>
     * Topics are split into lesson groups of at least 3 topics each (subject to the
     * total topic count), followed by a single checkpoint lesson that is not
     * associated with any topics. A chapter therefore always has at least 2 lessons:
     * one content lesson and the trailing checkpoint.
     * <p>
     * The number of content lessons is {@code floor(topicCount / 3)}, with a minimum
     * of 1. Remaining topics beyond an exact multiple of 3 are distributed round-robin
     * across the lesson groups, so no group differs from another by more than one topic,
     * and every group is guaranteed to have at least 3 topics whenever
     * {@code topicCount >= 3}. If the chapter has fewer than 3 topics in total, the
     * single content lesson simply contains all of them, since no valid grouping of
     * size 3 or more exists in that case.
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
        result.add(new Lesson(LessonType.CHECKPOINT, lessonCount, List.of()));
        this.lessons = result;
    }

    private static int computeLessonCount(int topicCount) {
        return Math.max(1, topicCount / 3);
    }

    private static List<List<Long>> distributeTopics(List<Long> topics, int lessonCount) {
        List<List<Long>> groups = new ArrayList<>(lessonCount);
        for (int i = 0; i < lessonCount; i++) {
            groups.add(new ArrayList<>());
        }
        for (int i = 0; i < topics.size(); i++) {
            groups.get(i % lessonCount).add(topics.get(i));
        }
        return groups;
    }
}
