package ch.supsi.textusbackend.model;

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

    private Integer page;

    private String title;

    private String summary;

    private List<String> keywords;

    private List<Entity> entities;

    private List<String> questions;

    private String key;

    @JsonAlias("related_keys")
    private List<String> relatedKeys;

    @Override
    public String toString() {
        return "Chunk{" + "tokenCountInput="
                + tokenCountInput + ", tokenCountOutput="
                + tokenCountOutput + ", page="
                + page + ", title='"
                + title + '\'' + ", summary='"
                + summary + '\'' + ", keywords="
                + keywords + ", entities="
                + entities + ", questions="
                + questions + ", key='"
                + key + '\'' + ", relatedKeys="
                + relatedKeys + '}';
    }
}
