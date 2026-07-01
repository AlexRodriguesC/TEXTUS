package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonDeserialize(using = MindMapNodeDeserializer.class)
public class MindMapNode {

    private Long topicId;

    private List<MindMapNode> children = new ArrayList<>();

    public MindMapNode(Long topicId) {
        this.topicId = topicId;
    }
}
