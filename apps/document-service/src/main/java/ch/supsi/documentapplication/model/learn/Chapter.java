package ch.supsi.documentapplication.model.learn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

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

    @Setter
    @JsonIgnore
    private List<Lesson2> lessons;

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
     * This method organizes the chapter's topics into a structured hierarchy of nodes.
     * If no topics are present, an empty nodes list is created. Otherwise, topics are
     * distributed across multiple groups based on a calculated step count, and each group
     * is converted into a Node. Lesson-type nodes contain distributed topic subsets,
     * while the final node is designated as a checkpoint node containing all topics.
     * <p>
     * The distribution of topics across nodes is determined by the topic count:
     * up to 3 topics result in 3 nodes, up to 6 topics result in 5 nodes, and
     * more than 6 topics result in 8 nodes.
     */
    public void buildNodes() {
        if (this.topics == null || this.topics.isEmpty()) {
            this.lessons = new ArrayList<>();
            return;
        }

        int stepCount = getStepCount(this.topics.size());
        List<List<Long>> groups = distributeTopics(this.topics, stepCount);

        List<Lesson2> result = new ArrayList<>(groups.size());
        for (int i = 0; i < groups.size(); i++) {
            LessonType type = (i == groups.size() - 1) ? LessonType.CHECKPOINT : LessonType.LESSON;
            if (type == LessonType.LESSON) {
                result.add(new Lesson2(type, i, groups.get(i)));
            } else {
                result.add(new Lesson2(type, i, this.topics));
            }
        }
        this.lessons = result;
    }

    private static int getStepCount(int topicCount) {
        if (topicCount <= 3) return topicCount;
        if (topicCount <= 6) return 5;
        return 8;
    }

    private static List<List<Long>> distributeTopics(List<Long> topics, int stepCount) {
        List<List<Long>> groups = new ArrayList<>(stepCount);
        for (int i = 0; i < stepCount; i++) {
            groups.add(new ArrayList<>());
        }
        for (int i = 0; i < topics.size(); i++) {
            groups.get(i % stepCount).add(topics.get(i));
        }
        return groups;
    }
}
