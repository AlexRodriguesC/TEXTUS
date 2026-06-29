package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Chunk {

    @JsonIgnore
    private Long id;

    @JsonIgnore
    private Long documentId;

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

    @Override
    public String toString() {
        return "Chunk{" + "id="
                + id + ", title='"
                + title + '\'' + ", summary='"
                + summary + '\'' + ", keywords="
                + keywords + ", key='"
                + key + '\'' + ", relatedKeys="
                + relatedKeys + '}';
    }
}
