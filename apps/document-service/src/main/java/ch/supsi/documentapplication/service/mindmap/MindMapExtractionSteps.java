package ch.supsi.documentapplication.service.mindmap;

import ch.supsi.documentapplication.model.LabelMindMapNode;
import ch.supsi.documentapplication.model.LabelTopicGroup;
import ch.supsi.documentapplication.service.async.learningpipeline.Label;
import java.util.List;

public interface MindMapExtractionSteps {

    Long selectRoot(List<Label> labels, List<LabelTopicGroup> groups);

    List<Long> selectChildren(LabelMindMapNode parent, List<Label> remaining, List<LabelTopicGroup> groups);
}
