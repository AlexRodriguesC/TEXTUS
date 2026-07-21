package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.exception.RetriesFailedException;
import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.AiUsage;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.StringUtils;

public abstract class Client<I, O> {

    protected static final String FORMAT = "format";

    protected ChatClient chatClient;
    protected BeanOutputConverter<O> converter;
    protected final String systemPrompt;
    protected final PromptTemplate promptTemplate;
    protected final RetryTemplateWrapper retryTemplate;
    protected final AiContext aiContext;

    public Client(
            ChatClient chatClient,
            BeanOutputConverter<O> converter,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            AiContext aiContext) {
        this.chatClient = chatClient;
        this.converter = converter;
        this.retryTemplate = retryTemplate;
        this.systemPrompt = systemPrompt;
        this.promptTemplate = promptTemplate;
        this.aiContext = aiContext;
    }

    protected Prompt getPrompt(Map<String, Object> params) {
        params.put(FORMAT, converter.getFormat());
        var filledUserPrompt = promptTemplate.render(params);
        var systemMessage = new SystemMessage(systemPrompt);
        var userMessage = UserMessage.builder().text(filledUserPrompt).build();
        return new Prompt(systemMessage, userMessage);
    }

    public ClientResult<O> getResult(I input) throws RetriesFailedException {
        return retryTemplate.execute(() -> call(input));
    }

    protected abstract ClientResult<O> call(I input);

    protected <T> ClientResult<T> invoke(Prompt prompt, Class<T> type) {
        var responseEntity = chatClient.prompt(prompt).call().responseEntity(type);
        return new ClientResult<>(responseEntity.getEntity(), extractUsage(responseEntity.getResponse()));
    }

    protected <T> ClientResult<T> invoke(Prompt prompt, ParameterizedTypeReference<T> type) {
        var responseEntity = chatClient.prompt(prompt).call().responseEntity(type);
        return new ClientResult<>(responseEntity.getEntity(), extractUsage(responseEntity.getResponse()));
    }

    protected ClientResult<O> withUsages(O result, List<AiUsage> usages) {
        return new ClientResult<>(result, usages);
    }

    protected AiUsage extractUsage(ChatResponse chatResponse) {
        var metadata = chatResponse.getMetadata();
        Usage usage = metadata.getUsage();

        int tokensIn = toInt(usage.getPromptTokens());
        int tokensOut = toInt(resolveCompletionTokens(usage));

        String model = StringUtils.hasText(metadata.getModel()) ? metadata.getModel() : aiContext.defaultModel();

        return new AiUsage(tokensIn, tokensOut, aiContext.provider(), model);
    }

    private static Integer resolveCompletionTokens(Usage usage) {
        if (usage == null) {
            return null;
        }
        return usage.getCompletionTokens();
    }

    private static int toInt(Integer value) {
        return value != null ? value : 0;
    }
}
