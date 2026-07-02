package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException;
import ch.supsi.documentapplication.model.MindMap;
import java.util.List;

public class MindMapValidator {

    public boolean validate(MindMap mindMap, List<Long> expectedChunkIds) {
        if (mindMap == null || expectedChunkIds == null) {
            throw new AIOutputMappingFailedException("Failed to validate MindMap");
        }

        var actualChunkIds = mindMap.getAllTopicIds();

        for (Long chunkId : expectedChunkIds) {
            if (!actualChunkIds.contains(chunkId)) {
                throw new AIOutputMappingFailedException("Failed to validate MindMap");
            }
        }
        return true;
    }
}
