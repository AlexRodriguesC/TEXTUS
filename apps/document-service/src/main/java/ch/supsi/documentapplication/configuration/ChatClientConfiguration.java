package ch.supsi.documentapplication.configuration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

@Configuration
public class ChatClientConfiguration {

    @Bean
    @Qualifier("interactiveChatClient")
    public ChatClient interactiveChatClient(
            ChatModel chatModel,
            ChatMemory chatMemory,
            @Value("${app.prompts.chat-system}") Resource systemPromptResource)
            throws IOException {
        String systemPrompt = FileCopyUtils.copyToString(
                new InputStreamReader(systemPromptResource.getInputStream(), StandardCharsets.UTF_8));

        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
