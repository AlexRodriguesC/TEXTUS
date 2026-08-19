package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Attempt;
import ch.supsi.documentapplication.model.learn.Lesson;
import ch.supsi.documentapplication.model.learn.LessonType;
import ch.supsi.documentapplication.model.learn.exercise.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseLearningStorage extends DatabaseStorage implements IDatabaseLearningStorage {

    private final ObjectMapper objectMapper;

    public DatabaseLearningStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<BaseExercise<?>> getExercises(Long lessonId) {
        List<BaseExercise<?>> exercises = new ArrayList<>();

        List<Map<String, Object>> rows = jdbcClient
                .sql("SELECT * FROM documents.read_exercises_by_lesson(:lessonId)")
                .param("lessonId", lessonId)
                .query()
                .listOfRows();

        for (Map<String, Object> row : rows) {
            Long exerciseId = ((Number) row.get("id")).longValue();
            Long topicId = ((Number) row.get("topic_id")).longValue();
            String family = (String) row.get("family");
            Integer index = ((Number) row.get("index")).intValue();
            String explanation = (String) row.get("explanation");
            boolean completed = (boolean) row.get("completed");

            BaseExercise<?> exercise =
                    switch (family) {
                        case "question" -> getQuestion(exerciseId);
                        case "list" -> getList(exerciseId);
                        case "group" -> getGroup(exerciseId);
                        case "pair" -> getPair(exerciseId);
                        default -> throw new IllegalArgumentException("Unsupported exercise family: " + family);
                    };

            exercise.setId(exerciseId);
            exercise.setTopicId(topicId);
            exercise.setIndex(index);
            exercise.setExplanation(explanation);
            exercise.setCompleted(completed);

            exercises.add(exercise);
        }

        return exercises;
    }

    private Question getQuestion(Long exerciseId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_question(:exerciseId)")
                .param("exerciseId", exerciseId)
                .query((rs, rowNum) -> {
                    Question q = new Question();
                    q.setId(rs.getLong("id"));
                    q.setRequest(rs.getString("question"));
                    q.setAnswer(rs.getString("answer"));
                    q.setDistractor(
                            Arrays.asList((String[]) rs.getArray("distractors").getArray()));
                    return q;
                })
                .single();
    }

    private ch.supsi.documentapplication.model.learn.exercise.List getList(Long exerciseId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_list(:exerciseId)")
                .param("exerciseId", exerciseId)
                .query((rs, rowNum) -> {
                    ch.supsi.documentapplication.model.learn.exercise.List l =
                            new ch.supsi.documentapplication.model.learn.exercise.List();
                    l.setId(rs.getLong("id"));
                    l.setRequest(rs.getString("request"));
                    l.setAnswer(Arrays.asList((String[]) rs.getArray("answer").getArray()));
                    return l;
                })
                .single();
    }

    private Group getGroup(Long exerciseId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_group(:exerciseId)")
                .param("exerciseId", exerciseId)
                .query((rs, rowNum) -> {
                    Group g = new Group();
                    g.setId(rs.getLong("group_exercise_id"));
                    g.setRequest(rs.getString("request"));

                    String answerJson = rs.getString("answer");
                    Map<String, java.util.List<String>> answer;
                    try {
                        answer = new ObjectMapper().readValue(answerJson, new TypeReference<>() {});
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse group answer JSON", e);
                    }
                    g.setAnswer(answer);
                    return g;
                })
                .single();
    }

    private PairExercise getPair(Long exerciseId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_pair(:exerciseId)")
                .param("exerciseId", exerciseId)
                .query((rs, rowNum) -> {
                    PairExercise p = new PairExercise();
                    p.setId(rs.getLong("id"));
                    p.setRequest(rs.getString("request"));

                    String pairsJson = rs.getString("pairs");
                    java.util.List<LeftRight> pairs;
                    try {
                        java.util.List<Map<String, String>> raw =
                                new ObjectMapper().readValue(pairsJson, new TypeReference<>() {});
                        pairs = raw.stream()
                                .map(m -> new LeftRight(m.get("left"), m.get("right")))
                                .toList();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse pairs JSON", e);
                    }
                    p.setAnswer(pairs);
                    return p;
                })
                .single();
    }

    @Override
    public BaseExercise<?> createExercise(BaseExercise<?> exercise, Long lessonId) {
        Long exerciseId = jdbcClient
                .sql("SELECT documents.create_exercise(:lessonId, :topicId, :family, :index, :explanation)")
                .param("lessonId", lessonId)
                .param("topicId", exercise.getTopicId())
                .param("family", exercise.getType().name().toLowerCase())
                .param("index", exercise.getIndex())
                .param("explanation", exercise.getExplanation())
                .query(Long.class)
                .single();

        switch (exercise) {
            case Question question -> createQuestion(exerciseId, question);
            case ch.supsi.documentapplication.model.learn.exercise.List listExercise -> createList(
                    exerciseId, listExercise);
            case Group group -> createGroup(exerciseId, group);
            case PairExercise pair -> createPair(exerciseId, pair);
            default -> throw new IllegalArgumentException("Unsupported exercise type: " + exercise.getClass());
        }
        exercise.setId(exerciseId);
        return exercise;
    }

    private void createQuestion(Long exerciseId, Question question) {
        Long id = jdbcClient
                .sql(
                        "SELECT documents.create_question(:exerciseId, :question::text, :answer::text, :distractors::text[])")
                .param("exerciseId", exerciseId)
                .param("question", question.getRequest())
                .param("answer", question.getAnswer())
                .param("distractors", toTextArray(question.getDistractor()))
                .query(Long.class)
                .single();
        question.setId(id);
    }

    private String toTextArray(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return "{"
                + String.join(
                        ",",
                        list.stream()
                                .map(item -> "\"" + item.replace("\"", "\\\"") + "\"")
                                .toList()) + "}";
    }

    private void createList(Long exerciseId, ch.supsi.documentapplication.model.learn.exercise.List list) {
        Long id = jdbcClient
                .sql("SELECT documents.create_list(:exerciseId, :request, :answer::text[])")
                .param("exerciseId", exerciseId)
                .param("request", list.getRequest())
                .param("answer", toTextArray(list.getAnswer()))
                .query(Long.class)
                .single();
        list.setId(id);
    }

    private void createGroup(Long exerciseId, Group group) {
        Long id = jdbcClient
                .sql("SELECT documents.create_group(:exerciseId, :request, :answer::jsonb)")
                .param("exerciseId", exerciseId)
                .param("request", group.getRequest())
                .param("answer", toGroupJson(group.getAnswer()))
                .query(Long.class)
                .single();
        group.setId(id);
    }

    private void createPair(Long exerciseId, PairExercise pair) {
        Long id = jdbcClient
                .sql("SELECT documents.create_pair(:exerciseId, :request, :pairs::jsonb)")
                .param("exerciseId", exerciseId)
                .param("request", pair.getRequest())
                .param("pairs", toPairsJson(pair.getAnswer()))
                .query(Long.class)
                .single();
        pair.setId(id);
    }

    @Override
    public void updateExercise(BaseExercise<?> exercise) {
        jdbcClient
                .sql(
                        "SELECT documents.update_exercise(:exerciseId, :topicId, :family, :index, :explanation, :completed, :correct)")
                .param("exerciseId", exercise.getId())
                .param("topicId", exercise.getTopicId())
                .param("family", exercise.getType().name().toLowerCase())
                .param("index", exercise.getIndex())
                .param("explanation", exercise.getExplanation())
                .param("completed", exercise.isCompleted())
                .param("correct", exercise.isCorrect())
                .query()
                .listOfRows();

        switch (exercise) {
            case Question question -> updateQuestion(exercise.getId(), question);
            case ch.supsi.documentapplication.model.learn.exercise.List listExercise -> updateList(
                    exercise.getId(), listExercise);
            case Group group -> updateGroup(exercise.getId(), group);
            case PairExercise pair -> updatePair(exercise.getId(), pair);
            default -> throw new IllegalArgumentException("Unsupported exercise type: " + exercise.getClass());
        }
    }

    @Override
    public void deleteExercises(Long lessonId) {
        jdbcClient
                .sql("SELECT documents.delete_exercises_by_lesson(:lessonId)")
                .param("lessonId", lessonId)
                .query()
                .listOfRows();
    }

    private void updateQuestion(Long exerciseId, Question question) {
        jdbcClient
                .sql("SELECT documents.update_question(:exerciseId, :question, :answer, :distractors::text[])")
                .param("exerciseId", exerciseId)
                .param("question", question.getRequest())
                .param("answer", question.getAnswer())
                .param("distractors", toTextArray(question.getDistractor()))
                .query()
                .listOfRows();
    }

    private void updateList(Long exerciseId, ch.supsi.documentapplication.model.learn.exercise.List list) {
        jdbcClient
                .sql("SELECT documents.update_list(:exerciseId::bigint, :request::text, :answer::text[])")
                .param("exerciseId", exerciseId)
                .param("request", list.getRequest())
                .param("answer", toTextArray(list.getAnswer()))
                .query()
                .listOfRows();
    }

    private void updateGroup(Long exerciseId, Group group) {
        jdbcClient
                .sql("SELECT documents.update_group(:exerciseId, :request, :answer::jsonb)")
                .param("exerciseId", exerciseId)
                .param("request", group.getRequest())
                .param("answer", toGroupJson(group.getAnswer()))
                .query()
                .listOfRows();
    }

    private void updatePair(Long exerciseId, PairExercise pair) {
        jdbcClient
                .sql("SELECT documents.update_pair(:exerciseId, :request, :pairs::jsonb)")
                .param("exerciseId", exerciseId)
                .param("request", pair.getRequest())
                .param("pairs", toPairsJson(pair.getAnswer()))
                .query()
                .listOfRows();
    }

    @Override
    public Lesson readLesson(Long lessonId) {
        Lesson lesson = jdbcClient
                .sql("SELECT * FROM documents.read_lesson(:lessonId)")
                .param("lessonId", lessonId)
                .query((rs, rowNum) -> {
                    Long id = rs.getLong("id");
                    Long chapterId = rs.getLong("chapter_id");
                    Integer index = rs.getInt("index");
                    LessonType type = LessonType.valueOf(rs.getString("type").toUpperCase());
                    Long currentTopic = rs.getLong("current_topic");
                    int attemptsLeft = rs.getInt("attempts_left");
                    double mastery = rs.getDouble("mastery");

                    List<Long> topics = new ArrayList<>(
                            Arrays.asList((Long[]) rs.getArray("topic_ids").getArray()));
                    return new Lesson(id, chapterId, index, type, topics, currentTopic, attemptsLeft, mastery);
                })
                .single();

        List<BaseExercise<?>> exercises = getExercises(lessonId);
        exercises.forEach(lesson::addExercise);

        return lesson;
    }

    @Override
    public void update(Lesson lesson) {
        Long[] topicIds =
                lesson.getTopics() == null ? new Long[0] : lesson.getTopics().toArray(new Long[0]);
        jdbcClient
                .sql("SELECT documents.update_lesson(:lessonId, :currentTopic, :attemptsLeft, :mastery, :topicIds)")
                .param("lessonId", lesson.getId())
                .param("currentTopic", lesson.getCurrentTopic())
                .param("attemptsLeft", lesson.getAttemptsLeft())
                .param("mastery", lesson.getMastery())
                .param("topicIds", topicIds)
                .query()
                .listOfRows();
    }

    @Override
    public void create(Attempt<?> attempt, String userId, Long lessonId, Long exerciseId) {

        String responseJson;
        try {
            responseJson = objectMapper.writeValueAsString(attempt.getUserResponse());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize attempt response", e);
        }

        jdbcClient
                .sql("SELECT * FROM documents.create_attempt(:exerciseId, :lessonId, :userId, :response::jsonb)")
                .param("exerciseId", exerciseId)
                .param("lessonId", lessonId)
                .param("userId", userId)
                .param("response", responseJson)
                .query()
                .listOfRows();
    }

    private String toGroupJson(Map<String, List<String>> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String toPairsJson(List<LeftRight> pairs) {
        try {
            var nodes = pairs.stream()
                    .map(p -> Map.of("left", p.left(), "right", p.right()))
                    .toList();
            return objectMapper.writeValueAsString(nodes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
