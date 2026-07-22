package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonClassDescription("The selected root page for a page-based mind map hierarchy.")
public class PageMindMapRootSelection {

    @JsonPropertyDescription(
            "The pageId of the input page that best represents the broadest subject among all provided pages.")
    private Long pageId;
}
