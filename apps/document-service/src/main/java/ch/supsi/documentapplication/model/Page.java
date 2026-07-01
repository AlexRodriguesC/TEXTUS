package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Page {

    @JsonIgnore
    private Long id;

    private String title;
    private String summary;
}
