package ch.supsi.documentapplication.service.clients;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

public abstract class OpenAIClient {

    protected ChatClient chatClient;

    public OpenAIClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    protected String getRawGeneration(@Nullable ChatResponse chatResponse) {
        return Optional.ofNullable(chatResponse)
                .map(ChatResponse::getResult)
                .map(Generation::getOutput)
                .map(AbstractMessage::getText)
                .orElse(null);
    }
}
