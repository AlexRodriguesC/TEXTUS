package ch.supsi.documentapplication.service.clients;

import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;

public abstract class Client<I, O> {

    protected ChatClient chatClient;

    private final PromptTemplate promptTemplate;
    private final RetryTemplate retryTemplate;

    public Client(ChatClient chatClient, RetryTemplate retryTemplate, PromptTemplate promptTemplate) {
        this.chatClient = chatClient;
        this.retryTemplate = retryTemplate;
        this.promptTemplate = promptTemplate;
    }

    protected Prompt getPrompt(Map<String, Object> params) {
        var filledUserPrompt = promptTemplate.render(params);
        var userPrompt = UserMessage.builder().text(filledUserPrompt).build();
        return new Prompt(userPrompt);
    }

    public O getResult(I input) throws RetryException {
        return retryTemplate.execute(() -> call(input));
    }

    protected abstract O call(I input);
}
