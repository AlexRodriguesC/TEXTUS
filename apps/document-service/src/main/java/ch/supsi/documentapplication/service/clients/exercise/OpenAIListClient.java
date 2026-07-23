package ch.supsi.documentapplication.service.clients.exercise;

import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.model.learn.exercise.List;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.clients.Client;
import ch.supsi.documentapplication.service.clients.EvaluationRestClient;
import java.util.HashMap;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

public class OpenAIListClient extends Client<Topic, List> {

    private static final String TOPIC = "topic";

    private final EvaluationRestClient evaluationRestClient;

    public OpenAIListClient(
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            AiContext aiContext,
            EvaluationRestClient evaluationRestClient) {
        super(
                chatClient,
                new BeanOutputConverter<>(List.class),
                retryTemplate,
                systemPrompt,
                promptTemplate,
                aiContext);
        this.evaluationRestClient = evaluationRestClient;
    }

    @Override
    protected ClientResult<List> call(Topic input) {
        Map<String, Object> params = new HashMap<>();
        params.put(TOPIC, input);
        ClientResult<List> result = invoke(getPrompt(params), List.class);
        evaluationRestClient.evaluateExercise(input, result.result());
        return result;
    }
}
