package ch.supsi.documentapplication.service.validator;

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException;
import ch.supsi.documentapplication.model.MindMap;
import java.util.List;

public class MindMapValidator implements Validator<MindMap, List<Long>> {

    public void validate(MindMap mindMap, List<Long> expectedChunkIds) {
        if (mindMap == null || expectedChunkIds == null) {
            throw AIOutputMappingFailedException.create("mind-map", "mind-map is null");
        }

        var actualChunkIds = mindMap.getAllTopicIds();

        for (Long chunkId : expectedChunkIds) {
            if (!actualChunkIds.contains(chunkId)) {
                throw AIOutputMappingFailedException.create(
                        "mind-map", "mind-map does not contain expected chunk id: " + chunkId);
            }
        }
    }
}
