package ch.supsi.documentapplication.service.clients.exercise;

import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.model.learn.exercise.Question;
import ch.supsi.documentapplication.service.clients.Client;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.retry.RetryTemplate;

public class OpenAIQuestionClient extends Client<Topic, Question> {

    public OpenAIQuestionClient(ChatClient chatClient, RetryTemplate retryTemplate, PromptTemplate promptTemplate) {
        super(chatClient, retryTemplate, promptTemplate);
    }

    @Override
    protected Question call(Topic input) {
        return null;
    }
}
