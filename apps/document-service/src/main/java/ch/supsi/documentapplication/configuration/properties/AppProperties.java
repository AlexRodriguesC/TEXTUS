package ch.supsi.documentapplication.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private String providerModelsUrl;

    private int maxDocumentSize;

    private int maxChunksPerStep;
    private int maxParallelism;
    private int maxChapters;

    private String tessdataPath;
    private int ocrThreshold;

    private String baseFilesystemPath;

    private String secretKey;
}
