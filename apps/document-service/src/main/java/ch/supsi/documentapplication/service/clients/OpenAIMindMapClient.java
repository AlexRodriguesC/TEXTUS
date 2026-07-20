package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.validator.MindMapValidator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;

public class OpenAIMindMapClient extends Client<List<Topic>, MindMap> {

    private static final String TOPICS = "topics";

    private final MindMapValidator mindMapValidator;

    public OpenAIMindMapClient(
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            MindMapValidator mindMapValidator) {
        super(chatClient, new BeanOutputConverter<>(MindMap.class), retryTemplate, systemPrompt, promptTemplate);
        this.mindMapValidator = mindMapValidator;
    }

    @Override
    protected MindMap call(List<Topic> input) {
        Map<String, Object> params = new HashMap<>();
        params.put(TOPICS, input);
        var prompt = getPrompt(params);

        var expectedChunkIds = input.stream().map(Topic::getId).toList();

        var map = chatClient.prompt(prompt).call().entity(MindMap.class);
        mindMapValidator.validate(map, expectedChunkIds);
        return map;
    }
}
