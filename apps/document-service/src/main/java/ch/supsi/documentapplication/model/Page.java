package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonClassDescription("A summarized document page, consisting of a concise title and a factual summary.")
public class Page {

    @JsonIgnore
    private Long id;

    @JsonIgnore
    private Integer pageNumber;

    @JsonPropertyDescription(
            "A concise title (5 to 15 words) accurately reflecting the page's main subject, grounded only in the source document.")
    private String title;

    @JsonPropertyDescription(
            "A factual summary of the page, written as direct declarative statements. Never use meta-descriptive phrasing like 'the document covers', 'this article discusses', or 'the text describes'.")
    private String summary;
}
