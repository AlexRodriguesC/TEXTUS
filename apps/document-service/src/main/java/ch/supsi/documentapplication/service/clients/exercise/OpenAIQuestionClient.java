package ch.supsi.documentapplication.service.clients.exercise;

import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.model.learn.exercise.Question;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.clients.Client;
import java.util.HashMap;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

public class OpenAIQuestionClient extends Client<Topic, Question> {

    public static final String TOPIC = "topic";

    public OpenAIQuestionClient(
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            AiContext aiContext) {
        super(
                chatClient,
                new BeanOutputConverter<>(Question.class),
                retryTemplate,
                systemPrompt,
                promptTemplate,
                aiContext);
    }

    @Override
    protected ClientResult<Question> call(Topic input) {
        Map<String, Object> params = new HashMap<>();
        params.put(TOPIC, input);
        return invoke(getPrompt(params), Question.class);
    }
}
