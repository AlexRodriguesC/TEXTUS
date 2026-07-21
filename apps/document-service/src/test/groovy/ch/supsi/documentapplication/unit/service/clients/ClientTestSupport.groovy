package ch.supsi.documentapplication.unit.service.clients

import ch.supsi.documentapplication.model.AiContext
import org.springframework.ai.chat.client.ResponseEntity
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.metadata.ChatResponseMetadata
import org.springframework.ai.chat.metadata.DefaultUsage
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation

class ClientTestSupport {

    static final List<String> TEST_MODELS = List.of("test-model", "fallback-model-2", "fallback-model-3")

    static final AiContext TEST_AI_CONTEXT = new AiContext("test-provider", "test-model", TEST_MODELS)

    static <T> ResponseEntity<ChatResponse, T> responseEntityFor(
            T entity,
            Integer promptTokens = 10,
            Integer completionTokens = 20) {
        def metadata = ChatResponseMetadata.builder()
                .model(TEST_AI_CONTEXT.defaultModel())
                .usage(new DefaultUsage(promptTokens, completionTokens))
                .build()
        def chatResponse = ChatResponse.builder()
                .generations([new Generation(new AssistantMessage("test"))])
                .metadata(metadata)
                .build()
        return new ResponseEntity<>(chatResponse, entity)
    }
}
