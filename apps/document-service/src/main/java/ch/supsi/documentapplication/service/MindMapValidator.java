package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.model.MindMap;
import java.util.List;

public class MindMapValidator {

    public boolean validate(MindMap mindMap, List<Long> expectedChunkIds) {
        if (mindMap == null || expectedChunkIds == null) {
            return false;
        }

        var actualChunkIds = mindMap.getAllChunkIds();

        for (Long chunkId : expectedChunkIds) {
            if (!actualChunkIds.contains(chunkId)) {
                return false;
            }
        }
        return true;
    }
}
