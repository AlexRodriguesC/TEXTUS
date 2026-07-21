package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonClassDescription("The selected root label for a label-based mind map hierarchy.")
public class LabelMindMapRootSelection {

    @JsonPropertyDescription(
            "The labelId of the input label that best represents the broadest subject among all provided labels.")
    private Long labelId;
}
