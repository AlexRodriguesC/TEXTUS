package ch.supsi.documentapplication.service.clients.exercise;

import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.model.learn.exercise.List;
import ch.supsi.documentapplication.service.clients.Client;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.retry.RetryTemplate;

public class OpenAIListClient extends Client<Topic, List> {

    public OpenAIListClient(ChatClient chatClient, RetryTemplate retryTemplate, PromptTemplate promptTemplate) {
        super(chatClient, retryTemplate, promptTemplate);
    }

    @Override
    protected List call(Topic input) {
        return null;
    }
}
