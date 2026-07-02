package ch.supsi.documentapplication.service.clients.mindmap;

import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.Topic;
import ch.supsi.documentapplication.service.MindMapValidator;
import ch.supsi.documentapplication.service.clients.FailureTracker;
import ch.supsi.documentapplication.service.clients.OpenAIClient;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;

@Slf4j
public class OpenAIMindMapClient extends OpenAIClient implements MindMapClient {

    private static final String TOPICS = "topics";

    private final PromptTemplate promptTemplate;
    private final RetryTemplate retryTemplate;
    private final FailureTracker failureTracker;
    private final MindMapValidator mindMapValidator;

    public OpenAIMindMapClient(
            ChatClient chatClient,
            RetryTemplate retryTemplate,
            FailureTracker failureTracker,
            MindMapValidator mindMapValidator,
            PromptTemplate promptTemplate) {
        super(chatClient);
        this.retryTemplate = retryTemplate;
        this.promptTemplate = promptTemplate;
        this.failureTracker = failureTracker;
        this.mindMapValidator = mindMapValidator;
    }

    @Override
    public MindMap extractMindMap(List<Topic> topics) throws RetryException {
        var filledUserPrompt = promptTemplate.render(Map.of(TOPICS, topics));

        var userPrompt = UserMessage.builder().text(filledUserPrompt).build();
        var prompt = new Prompt(userPrompt);

        var expectedChunkIds = topics.stream().map(Topic::getId).toList();

        MindMap mindMap = retryTemplate.execute(() -> {
            var map = chatClient.prompt(prompt).call().entity(MindMap.class);
            mindMapValidator.validate(map, expectedChunkIds);
            return map;
        });

        failureTracker.resetFailures();

        return mindMap;
    }
}
