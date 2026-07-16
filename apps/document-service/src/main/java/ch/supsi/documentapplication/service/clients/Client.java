package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.exception.RetriesFailedException;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

public abstract class Client<I, O> {

    protected ChatClient chatClient;

    private final PromptTemplate promptTemplate;
    private final RetryTemplateWrapper retryTemplate;

    public Client(ChatClient chatClient, RetryTemplateWrapper retryTemplate, PromptTemplate promptTemplate) {
        this.chatClient = chatClient;
        this.retryTemplate = retryTemplate;
        this.promptTemplate = promptTemplate;
    }

    protected Prompt getPrompt(Map<String, Object> params) {
        var filledUserPrompt = promptTemplate.render(params);
        var userPrompt = UserMessage.builder().text(filledUserPrompt).build();
        return new Prompt(userPrompt);
    }

    public O getResult(I input) throws RetriesFailedException {
        return retryTemplate.execute(() -> call(input));
    }

    protected abstract O call(I input);
}
