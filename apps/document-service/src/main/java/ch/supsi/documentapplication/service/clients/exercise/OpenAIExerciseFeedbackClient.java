package ch.supsi.documentapplication.service.clients.exercise;

import ch.supsi.documentapplication.model.Attempt;
import ch.supsi.documentapplication.model.Feedback;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.clients.Client;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.retry.RetryOperations;

import java.util.Map;

public class OpenAIExerciseFeedbackClient extends Client<Attempt<?>, String> {


    public static final String EXERCISE_TYPE = "exercise_type";
    public static final String EXERCISE = "exercise";
    public static final String USER_RESPONSE = "user_response";
    public static final String CORRECT_ANSWER = "correct_answer";
    public static final String TOPIC = "topic";

    public OpenAIExerciseFeedbackClient(ChatClient chatClient, RetryTemplateWrapper retryTemplate, PromptTemplate promptTemplate) {
        super(chatClient, retryTemplate, promptTemplate);
    }

    @Override
    protected String call(Attempt<?> attempt) {
        Map<String, Object> params = Map.ofEntries(
                Map.entry(EXERCISE_TYPE, attempt.getExerciseType()),
                Map.entry(EXERCISE, attempt.getExercise()),
                Map.entry(USER_RESPONSE, attempt.getUserResponse()),
                Map.entry(CORRECT_ANSWER, attempt.getAnswer()),
                Map.entry(TOPIC, attempt.getTopic())
        );
        var prompt = getPrompt(params);

        return chatClient.prompt(prompt).call().entity(Feedback.class).getFeedback();
    }
}
