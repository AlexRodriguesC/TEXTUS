package ch.supsi.documentapplication.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
