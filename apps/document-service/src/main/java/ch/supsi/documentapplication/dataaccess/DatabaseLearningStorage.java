package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Attempt;
import ch.supsi.documentapplication.model.learn.Lesson2;
import ch.supsi.documentapplication.model.learn.exercise.BaseExercise;

import java.util.List;

import ch.supsi.documentapplication.model.learn.exercise.Group;
import ch.supsi.documentapplication.model.learn.exercise.Pair;
import ch.supsi.documentapplication.model.learn.exercise.Question;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseLearningStorage extends DatabaseStorage implements IDatabaseLearningStorage {

    public DatabaseLearningStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public List<BaseExercise<?>> getExercises(Long spaceId, Long nodeId) {
        return List.of();
    }

    @Override
    public BaseExercise<?> createExercise(BaseExercise<?> exercise, Long lessonId) {
        Long exerciseId = jdbcClient
                .sql("SELECT documents.create_exercise(:lessonId, :topicId, :family, :index)")
                .param("lessonId", lessonId)
                .param("topicId", exercise.getTopicId())
                .param("family", exercise.getType().name().toLowerCase())
                .param("index", exercise.getIndex())
                .query(Long.class)
                .single();
        exercise.setId(exerciseId);

        switch (exercise) {
            case Question question -> createQuestion(exerciseId, question);
            case ch.supsi.documentapplication.model.learn.exercise.List listExercise ->
                    createList(exerciseId, listExercise);
            case Group group -> createGroup(exerciseId, group);
            case Pair pair -> createPair(exerciseId, pair);
            default -> throw new IllegalArgumentException("Unsupported exercise type: " + exercise.getClass());
        }
        return exercise;
    }

    private void createQuestion(Long exerciseId, Question question) {
        Long id = jdbcClient
                .sql("SELECT documents.create_question(:exerciseId, :question, :answer, :explanation, :distractors)")
                .param("exerciseId", exerciseId)
                .param("question", question.getRequest())
                .param("answer", question.getAnswer())
                .param("explanation", question.getExplanation())
                .param("distractors", toTextArray(question.getDistractor()))
                .query(Long.class)
                .single();
        question.setId(id);
    }

    private void createList(Long exerciseId, ch.supsi.documentapplication.model.learn.exercise.List list) {
        Long id = jdbcClient
                .sql("SELECT documents.create_list(:exerciseId, :request, :answer, :explanation)")
                .param("exerciseId", exerciseId)
                .param("request", list.getRequest())
                .param("answer", toTextArray(list.getAnswer()))
                .param("explanation", list.getExplanation())
                .query(Long.class)
                .single();
        list.setId(id);
    }

    private void createGroup(Long exerciseId, Group group) {
        Long id = jdbcClient
                .sql("SELECT documents.create_group(:exerciseId, :request, :explanation, :answer::jsonb)")
                .param("exerciseId", exerciseId)
                .param("request", group.getRequest())
                .param("explanation", group.getExplanation())
                .param("answer", toJson(group.getAnswer()))
                .query(Long.class)
                .single();
        group.setId(id);
    }

    private void createPair(Long exerciseId, Pair pair) {
        var answer = pair.getAnswer();
        Long id = jdbcClient
                .sql("SELECT documents.create_pair(:exerciseId, :request, :leftValue, :rightValue, :explanation)")
                .param("exerciseId", exerciseId)
                .param("request", pair.getRequest())
                .param("leftValue", answer.getLeft())
                .param("rightValue", answer.getRight())
                .param("explanation", pair.getExplanation())
                .query(Long.class)
                .single();
        pair.setId(id);
    }

    @Override
    public void updateExercise(BaseExercise<?> exercise) {}

    @Override
    public Lesson2 readLesson(Long lessonId) {
        return null;
    }

    @Override
    public void update(Lesson2 lesson) {
        // call update update_lesson
        // for each exercise call update_exercise

    }

    @Override
    public void create(Attempt<?> attempt, String userId, Long lessonId, Long exerciseId) {
        jdbcClient
                .sql(
                        "SELECT * FROM documents.create_attempt(:exerciseId, :lessonId, :userId, :response::jsonb, :isCorrect, :feedback)")
                .param("exerciseId", exerciseId)
                .param("lessonId", lessonId)
                .param("userId", userId)
                .param("response", attempt.getUserResponse())
                .param("isCorrect", !attempt.isAnswerWrong())
                .param("feedback", attempt.getFeedback())
                .update();
    }
}
