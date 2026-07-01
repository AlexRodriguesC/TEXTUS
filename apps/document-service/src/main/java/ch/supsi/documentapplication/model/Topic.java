package ch.supsi.documentapplication.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Topic {

    private List<String> labels;
    private String title;
    private String summary;
}
