package ch.supsi.textusbackend.configuration;

import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class BatchConfiguration extends DefaultBatchConfiguration {

    @Override
    protected @NotNull TaskExecutor getTaskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
}
