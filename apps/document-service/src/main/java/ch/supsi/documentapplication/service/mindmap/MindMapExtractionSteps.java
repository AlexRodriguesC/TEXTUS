package ch.supsi.documentapplication.service.mindmap;

import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.PageMindMapNode;
import ch.supsi.documentapplication.model.PageTopicGroup;
import java.util.List;

public interface MindMapExtractionSteps {

    Long selectRoot(List<Page> pages, List<PageTopicGroup> groups);

    List<Long> selectChildren(PageMindMapNode parent, List<Page> remaining, List<PageTopicGroup> groups);
}
