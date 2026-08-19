package ch.supsi.documentapplication.service.clients.exercise;

import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.Attempt;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.Feedback;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.clients.Client;
import java.util.HashMap;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

public class OpenAIExerciseFeedbackClient extends Client<Attempt<?>, String> {

    public static final String EXERCISE_TYPE = "exercise_type";
    public static final String EXERCISE = "exercise";
    public static final String USER_RESPONSE = "user_response";
    public static final String CORRECT_ANSWER = "correct_answer";
    public static final String TOPIC = "topic";

    public OpenAIExerciseFeedbackClient(
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            AiContext aiContext) {
        super(
                chatClient,
                new BeanOutputConverter<>(String.class),
                retryTemplate,
                systemPrompt,
                promptTemplate,
                aiContext);
    }

    @Override
    protected ClientResult<String> call(Attempt<?> attempt) {
        Map<String, Object> params = new HashMap<>();
        params.put(EXERCISE_TYPE, attempt.getExerciseType());
        params.put(EXERCISE, attempt.getExercise());
        params.put(USER_RESPONSE, attempt.getUserResponse());
        params.put(CORRECT_ANSWER, attempt.getAnswer());
        params.put(TOPIC, attempt.getTopic());

        var result = invoke(getPrompt(params), Feedback.class);
        return new ClientResult<>(result.result().getFeedback(), result.usages());
    }
}
