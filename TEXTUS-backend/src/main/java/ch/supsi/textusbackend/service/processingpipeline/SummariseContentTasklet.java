package ch.supsi.textusbackend.service.processingpipeline;

import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@StepScope
public class SummariseContentTasklet implements Tasklet {

    private final ChatClient chatClient;

    @Value("#{jobParameters['BOOK_ID']}") // Injected from job params
    private String bookId;

    @Autowired
    public SummariseContentTasklet(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) {
        log.debug("Executing summarise step for book with bookId {}", bookId);

        return null;
    }
}
