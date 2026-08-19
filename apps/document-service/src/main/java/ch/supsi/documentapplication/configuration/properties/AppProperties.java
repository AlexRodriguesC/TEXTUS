<<<<<<<< HEAD:apps/document-service/src/main/java/ch/supsi/documentapplication/properties/AppProperties.java
package ch.supsi.documentapplication.properties;
========
package ch.supsi.documentapplication.configuration.properties;
>>>>>>>> origin/feature/testing_service:apps/document-service/src/main/java/ch/supsi/documentapplication/configuration/properties/AppProperties.java

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

    private String tessdataPath;
    private int ocrThreshold;

    private String baseFilesystemPath;

    private String secretKey;
}
