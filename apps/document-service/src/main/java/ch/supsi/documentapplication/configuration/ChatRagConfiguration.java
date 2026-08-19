package ch.supsi.documentapplication.configuration;

import ch.supsi.documentapplication.configuration.properties.PromptProperties;
import ch.supsi.documentapplication.exception.PromptTemplateLoadException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

@Configuration
public class ChatRagConfiguration {

    @Bean
    @Qualifier("ragQueryChatClientBuilder")
    public ChatClient.Builder ragQueryChatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(OpenAiChatOptions.builder().temperature(0.0));
    }

    /* Spring AI's documentation suggests setting transformer's temperature to 0.0 for better results https://docs.spring.io/spring-ai/reference/api/retrieval-augmented-generation.html#_query_transformation*/

    @Bean
    public CompressionQueryTransformer compressionQueryTransformer(
            @Qualifier("ragQueryChatClientBuilder") ChatClient.Builder ragQueryChatClientBuilder) {
        return CompressionQueryTransformer.builder()
                .chatClientBuilder(ragQueryChatClientBuilder)
                .build();
    }

    @Bean
    public RewriteQueryTransformer rewriteQueryTransformer(
            @Qualifier("ragQueryChatClientBuilder") ChatClient.Builder ragQueryChatClientBuilder) {
        return RewriteQueryTransformer.builder()
                .chatClientBuilder(ragQueryChatClientBuilder)
                .build();
    }

    @Bean
    public QueryAugmenter chatRagQueryAugmenter(
            StTemplateRenderer templateRenderer, PromptProperties promptProperties, ResourceLoader resourceLoader) {
        PromptTemplate promptTemplate =
                loadPromptTemplate(resourceLoader.getResource(promptProperties.getChatRagUser()), templateRenderer);
        PromptTemplate emptyContextPromptTemplate = loadPromptTemplate(
                resourceLoader.getResource(promptProperties.getChatRagEmptyContext()), templateRenderer);

        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .promptTemplate(promptTemplate)
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();
    }

    private static PromptTemplate loadPromptTemplate(Resource resource, StTemplateRenderer templateRenderer) {
        try {
            String template = FileCopyUtils.copyToString(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            return PromptTemplate.builder()
                    .renderer(templateRenderer)
                    .template(template)
                    .build();
        } catch (IOException e) {
            throw new PromptTemplateLoadException(
                    "Failed to load RAG prompt template: " + resource.getDescription(), e);
        }
    }
}
