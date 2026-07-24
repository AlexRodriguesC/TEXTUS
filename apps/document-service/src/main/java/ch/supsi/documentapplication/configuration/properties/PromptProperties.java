package ch.supsi.documentapplication.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.prompts")
@Getter
@Setter
public class PromptProperties {

    private String summarizationSystem;
    private String summarizationUser;

    private String topicExtractionSystem;
    private String topicExtractionUser;

    private String mindMapExtractionRootSystem;
    private String mindMapExtractionRootUser;
    private String mindMapExtractionChildrenSystem;
    private String mindMapExtractionChildrenUser;

    private String mindMapExtractionFeedbackSystem;
    private String mindMapExtractionFeedbackUser;

    private String chapterExtractionSystem;
    private String chapterExtractionUser;

    private String chapterExtractionFeedbackSystem;
    private String chapterExtractionFeedbackUser;

    private String groupSystem;
    private String groupUser;

    private String listSystem;
    private String listUser;

    private String pairSystem;
    private String pairUser;

    private String questionSystem;
    private String questionUser;

    private String feedbackSystem;
    private String feedbackUser;

    private String chatRagUser;
    private String chatRagEmptyContext;
}
