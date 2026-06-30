package ch.supsi.documentapplication.service.clients;

import java.util.Map;
import java.util.Optional;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import tools.jackson.core.exc.StreamReadException;

public abstract class Client<I, O> extends OpenAIClient {

    private final PromptTemplate promptTemplate;
    private final FailureTracker failureTracker;

    public Client(ChatClient chatClient, PromptTemplate promptTemplate, FailureTracker failureTracker) {
        super(chatClient);
        this.promptTemplate = promptTemplate;
        this.failureTracker = failureTracker;
    }

    protected Prompt getPrompt(Map<String, Object> params) {
        var filledUserPrompt = promptTemplate.render(params);
        var userPrompt = UserMessage.builder().text(filledUserPrompt).build();
        return new Prompt(userPrompt);
    }

    public O getResult(I input) {
        Optional<O> result = Optional.empty();

        while (result.isEmpty()) {
            try {
                result = call(input);
            } catch (StreamReadException e) {
                failureTracker.registerFailure();
                continue;
            }

            if (result.isEmpty()) {
                failureTracker.registerFailure();
            }
        }

        failureTracker.resetFailures();

        return result.get();
    }

    protected abstract Optional<O> call(I input);
}
