package ch.supsi.documentapplication.service.clients.exercise;

import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.model.learn.exercise.PairExercise;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.clients.Client;
import java.util.HashMap;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

public class OpenAIPairClient extends Client<Topic, PairExercise> {

    private static final String TOPIC = "topic";

    public OpenAIPairClient(
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate) {
        super(chatClient, new BeanOutputConverter<>(PairExercise.class), retryTemplate, systemPrompt, promptTemplate);
    }

    @Override
    protected PairExercise call(Topic input) {
        Map<String, Object> params = new HashMap<>();
        params.put(TOPIC, input);
        var prompt = getPrompt(params);
        return chatClient.prompt(prompt).call().entity(PairExercise.class);
    }
}
