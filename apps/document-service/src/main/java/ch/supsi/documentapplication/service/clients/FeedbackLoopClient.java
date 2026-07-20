package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

public abstract class FeedbackLoopClient<I, O> extends Client<I, O> {

    protected final String feedbackSystemPrompt;
    protected final PromptTemplate feedbackPromptTemplate;

    public FeedbackLoopClient(
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            String feedbackSystemPrompt,
            PromptTemplate feedbackPromptTemplate) {
        super(chatClient, retryTemplate, systemPrompt, promptTemplate);
        this.feedbackSystemPrompt = feedbackSystemPrompt;
        this.feedbackPromptTemplate = feedbackPromptTemplate;
    }

    protected Prompt getFeedbackPrompt(Map<String, Object> params) {
        var filledUserPrompt = feedbackPromptTemplate.render(params);
        var userPrompt = UserMessage.builder().text(filledUserPrompt).build();
        return new Prompt(userPrompt);
    }
}
