package ch.supsi.documentapplication.service.clients;

import org.springframework.ai.chat.client.ChatClient;

public abstract class OpenAIClient {

    protected ChatClient chatClient;

    public OpenAIClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
}
