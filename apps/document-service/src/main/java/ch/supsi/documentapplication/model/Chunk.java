package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Chunk {

    @JsonIgnore
    private Integer tokenCountInput;

    @JsonIgnore
    private Integer tokenCountOutput;

    private String title;

    private String summary;

    private List<String> keywords;

    private String key;

    @JsonAlias("related_keys")
    private List<String> relatedKeys;
}
