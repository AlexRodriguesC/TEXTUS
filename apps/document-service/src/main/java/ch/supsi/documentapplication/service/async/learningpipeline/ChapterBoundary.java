package ch.supsi.documentapplication.service.async.learningpipeline;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonClassDescription(
        "A single chapter boundary detected within an ordered window of topics. Topics from position 1 through cuttingPoint inclusive form one coherent chapter.")
public class ChapterBoundary {

    @JsonPropertyDescription("A concise chapter title, 2 to 3 words, plain text, no punctuation beyond spaces.")
    private final String title;

    @JsonPropertyDescription(
            "A concise chapter description, 6 to 7 words, plain text, summarizing the chapter's focus.")
    private final String description;

    @JsonPropertyDescription(
            "Inclusive 1-based position index within the current window marking the last topic of this chapter. Must be between 1 and the window size inclusive.")
    private final Integer cuttingPoint;
}
