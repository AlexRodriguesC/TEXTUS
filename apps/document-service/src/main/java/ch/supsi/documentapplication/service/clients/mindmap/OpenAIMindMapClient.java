package ch.supsi.documentapplication.service.clients.mindmap;

import ch.supsi.documentapplication.model.Chunk;
import ch.supsi.documentapplication.model.MindMapNode;
import ch.supsi.documentapplication.service.clients.FailureTracker;
import ch.supsi.documentapplication.service.clients.OpenAIClient;
import ch.supsi.documentapplication.service.clients.StringSanitizer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class OpenAIMindMapClient extends OpenAIClient implements MindMapClient {

    private final PromptTemplate promptTemplate;
    private final FailureTracker failureTracker;
    private final StringSanitizer stringSanitizer;

    public OpenAIMindMapClient(
            ChatClient chatClient,
            FailureTracker failureTracker,
            StringSanitizer stringSanitizer,
            PromptTemplate promptTemplate) {
        super(chatClient);
        this.promptTemplate = promptTemplate;
        this.failureTracker = failureTracker;
        this.stringSanitizer = stringSanitizer;
    }

    @Override
    public MindMapNode extractMindMap(List<Chunk> chunks) {
        var filledUserPrompt = promptTemplate.render(Map.of(
                "chunks", chunks
        ));

        var userPrompt = UserMessage.builder().text(filledUserPrompt).build();
        var prompt = new Prompt(userPrompt);

        boolean successful = false;
        MindMapNode mindMapNode = null;

        while (!successful) {
            mindMapNode = chatClient.prompt(prompt).call().entity(MindMapNode.class);


            successful = true;
        }

        failureTracker.resetFailures();

        return null;
    }

}
