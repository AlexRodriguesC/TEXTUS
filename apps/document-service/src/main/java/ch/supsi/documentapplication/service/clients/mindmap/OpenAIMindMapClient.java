package ch.supsi.documentapplication.service.clients.mindmap;

import ch.supsi.documentapplication.model.Chunk;
import ch.supsi.documentapplication.model.MindMap;
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

@Slf4j
public class OpenAIMindMapClient extends OpenAIClient implements MindMapClient {

    private final PromptTemplate promptTemplate;
    private final FailureTracker failureTracker;
    private final MindMapValidator mindMapValidator;

    public OpenAIMindMapClient(
            ChatClient chatClient,
            FailureTracker failureTracker,
            MindMapValidator mindMapValidator,
            PromptTemplate promptTemplate) {
        super(chatClient);
        this.promptTemplate = promptTemplate;
        this.failureTracker = failureTracker;
        this.mindMapValidator = mindMapValidator;
    }

    @Override
    public MindMap extractMindMap(List<Chunk> chunks) {
        var filledUserPrompt = promptTemplate.render(Map.of("chunks", chunks));

        var userPrompt = UserMessage.builder().text(filledUserPrompt).build();
        var prompt = new Prompt(userPrompt);

        boolean successful = false;

        var expectedChunkIds = chunks.stream().map(Chunk::getId).toList();
        MindMap mindMap = null;

        while (!successful) {
            mindMap = chatClient.prompt(prompt).call().entity(MindMap.class);

            successful = mindMapValidator.validate(mindMap, expectedChunkIds);

            if (!successful) {
                failureTracker.registerFailure();
            }
        }

        failureTracker.resetFailures();

        return mindMap;
    }
}
