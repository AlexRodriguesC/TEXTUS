package ch.supsi.documentapplication.service.clients.exercise;

import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.model.learn.exercise.Group;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.clients.Client;
import java.util.HashMap;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

public class OpenAIGroupClient extends Client<Topic, Group> {

    private static final String TOPIC = "topic";

    public OpenAIGroupClient(
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            AiContext aiContext) {
        super(
                chatClient,
                new BeanOutputConverter<>(Group.class),
                retryTemplate,
                systemPrompt,
                promptTemplate,
                aiContext);
    }

    @Override
    protected ClientResult<Group> call(Topic input) {
        Map<String, Object> params = new HashMap<>();
        params.put(TOPIC, input);
        return invoke(getPrompt(params), Group.class);
    }
}
