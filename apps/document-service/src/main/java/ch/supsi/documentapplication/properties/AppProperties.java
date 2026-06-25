package ch.supsi.documentapplication.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private int maxDocumentSize;

    private int maxChunksPerStep;

    private String tessdataPath;
    private int ocrThreshold;

    private String baseFilesystemPath;

    private int maxRollingKeySize;

    private int maxLLMFailures;

    private String secretKey;
}
