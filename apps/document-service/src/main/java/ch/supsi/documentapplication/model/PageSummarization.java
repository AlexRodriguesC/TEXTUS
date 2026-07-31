package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonClassDescription(
        "Classification and optional summary for a single document page. Non-content pages are identified before summarization.")
public class PageSummarization {

    @JsonPropertyDescription(
            "true when the page contains substantive educational or informational content; false for covers, title pages, copyright or imprint pages, tables of contents, indexes, acknowledgments, dedications, bibliographies or reference lists, glossaries, appendices, and blank or boilerplate-only pages.")
    private boolean contentPage;

    @JsonPropertyDescription(
            "A concise title (5 to 15 words) accurately reflecting the page's main subject. Required and non-empty when contentPage is true; otherwise an empty string.")
    private String title;

    @JsonPropertyDescription(
            "A factual summary of the page written as direct declarative statements. Required and non-empty when contentPage is true; otherwise an empty string.")
    private String summary;
}
