package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonClassDescription("Direct child labels assigned under a single parent node in the hierarchy.")
public class LabelMindMapChildSelection {

    @JsonPropertyDescription(
            "labelIds from the remaining label set that are immediate direct children of the parent label. May be empty.")
    private List<Long> labelIds = new ArrayList<>();
}
