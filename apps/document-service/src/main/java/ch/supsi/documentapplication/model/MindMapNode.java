package ch.supsi.documentapplication.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class MindMapNode {

    private Long chunkId;
    private List<MindMapNode> children = new ArrayList<>();

    public MindMapNode(Long chunkId) {
        this.chunkId = chunkId;
    }
}
