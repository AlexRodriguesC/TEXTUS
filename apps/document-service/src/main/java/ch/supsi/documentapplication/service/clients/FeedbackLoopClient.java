package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

public abstract class FeedbackLoopClient<I, O> extends Client<I, O> {

    protected final String feedbackSystemPrompt;
    protected final PromptTemplate feedbackPromptTemplate;

    public FeedbackLoopClient(
            ChatClient chatClient,
            BeanOutputConverter<O> converter,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            String feedbackSystemPrompt,
            PromptTemplate feedbackPromptTemplate,
            AiContext aiContext) {
        super(chatClient, converter, retryTemplate, systemPrompt, promptTemplate, aiContext);
        this.feedbackSystemPrompt = feedbackSystemPrompt;
        this.feedbackPromptTemplate = feedbackPromptTemplate;
    }

    protected Prompt getFeedbackPrompt(Map<String, Object> params) {
        params.put(FORMAT, converter.getFormat());
        var filledUserPrompt = feedbackPromptTemplate.render(params);
        var userPrompt = UserMessage.builder().text(filledUserPrompt).build();
        return new Prompt(userPrompt);
    }
}
