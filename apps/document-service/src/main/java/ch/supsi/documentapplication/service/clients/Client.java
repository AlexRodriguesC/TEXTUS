package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.exception.RetriesFailedException;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

public abstract class Client<I, O> {

    protected static final String FORMAT = "format";

    protected ChatClient chatClient;
    protected BeanOutputConverter<O> converter;
    protected final String systemPrompt;
    protected final PromptTemplate promptTemplate;
    protected final RetryTemplateWrapper retryTemplate;

    public Client(
            ChatClient chatClient,
            BeanOutputConverter<O> converter,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate) {
        this.chatClient = chatClient;
        this.converter = converter;
        this.retryTemplate = retryTemplate;
        this.systemPrompt = systemPrompt;
        this.promptTemplate = promptTemplate;
    }

    protected Prompt getPrompt(Map<String, Object> params) {
        params.put(FORMAT, converter.getFormat());
        var filledUserPrompt = promptTemplate.render(params);
        var systemMessage = new SystemMessage(systemPrompt);
        var userMessage = UserMessage.builder().text(filledUserPrompt).build();
        return new Prompt(systemMessage, userMessage);
    }

    public O getResult(I input) throws RetriesFailedException {
        return retryTemplate.execute(() -> call(input));
    }

    protected abstract O call(I input);
}
