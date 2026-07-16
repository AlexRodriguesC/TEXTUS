package ch.supsi.documentapplication.service.clients.exercise;

import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.model.learn.exercise.Pair;
import ch.supsi.documentapplication.service.clients.Client;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.retry.RetryTemplate;

public class OpenAIPairClient extends Client<Topic, Pair> {

    public OpenAIPairClient(ChatClient chatClient, RetryTemplate retryTemplate, PromptTemplate promptTemplate) {
        super(chatClient, retryTemplate, promptTemplate);
    }

    @Override
    protected Pair call(Topic input) {
        return null;
    }
}
