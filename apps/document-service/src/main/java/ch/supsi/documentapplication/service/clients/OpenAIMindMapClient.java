package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.Topic;
import ch.supsi.documentapplication.service.MindMapValidator;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.retry.RetryTemplate;

@Log4j2
public class OpenAIMindMapClient extends Client<List<Topic>, MindMap> {

    private static final String TOPICS = "topics";

    private final MindMapValidator mindMapValidator;

    public OpenAIMindMapClient(
            ChatClient chatClient,
            RetryTemplate retryTemplate,
            MindMapValidator mindMapValidator,
            PromptTemplate promptTemplate) {
        super(chatClient, retryTemplate, promptTemplate);
        this.mindMapValidator = mindMapValidator;
    }

    @Override
    protected MindMap call(List<Topic> input) {
        var prompt = getPrompt(Map.of(TOPICS, input));

        var expectedChunkIds = input.stream().map(Topic::getId).toList();

        var map = chatClient.prompt(prompt).call().entity(MindMap.class);
        mindMapValidator.validate(map, expectedChunkIds);
        return map;
    }
}
