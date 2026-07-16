package ch.supsi.documentapplication.service.clients.exercise;

import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.model.learn.exercise.List;
import ch.supsi.documentapplication.model.learn.exercise.Question;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.clients.Client;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.retry.RetryOperations;
import org.springframework.core.retry.RetryTemplate;

import java.util.Map;

public class OpenAIListClient extends Client<Topic, List> {

    private static final String TOPIC = "topic";

    public OpenAIListClient(ChatClient chatClient, RetryTemplateWrapper retryTemplate, PromptTemplate promptTemplate) {
        super(chatClient, retryTemplate, promptTemplate);
    }

    @Override
    protected List call(Topic input) {
        Map<String, Object> params = Map.of(TOPIC, input);
        var prompt = getPrompt(params);
        return chatClient.prompt(prompt).call().entity(List.class);
    }
}
