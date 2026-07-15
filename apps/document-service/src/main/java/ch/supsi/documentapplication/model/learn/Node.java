package ch.supsi.documentapplication.model.learn;

import java.util.List;
import lombok.Getter;

@Getter
public class Node {

    private Long id;
    private NodeType type;
    private Integer index;
    private List<Long> topics;

    public Node(Long id, NodeType type, Integer index, List<Long> topics) {
        this.id = id;
        this.type = type;
        this.index = index;
        this.topics = topics;
    }

    public Node(NodeType type, Integer index, List<Long> topics) {
        this.type = type;
        this.index = index;
        this.topics = topics;
    }
}
