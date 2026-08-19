package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MindMapNodeDeserializer extends JsonDeserializer<MindMapNode> {

    @Override
    public MindMapNode deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        ObjectNode node = mapper.readTree(p);

        MindMapNode mindMapNode = new MindMapNode();
        mindMapNode.setType(MindMapNodeType.valueOf(node.get("type").asText()));

        if (node.has("pageId") && !node.get("pageId").isNull()) {
            mindMapNode.setPageId(node.get("pageId").asLong());
        }
        if (node.has("topicId") && !node.get("topicId").isNull()) {
            mindMapNode.setTopicId(node.get("topicId").asLong());
        }

        if (node.has("children") && node.get("children").isArray()) {
            List<MindMapNode> children = mapper.treeToValue(
                    node.get("children"),
                    mapper.getTypeFactory().constructCollectionType(List.class, MindMapNode.class));
            mindMapNode.setChildren(children);
        } else {
            mindMapNode.setChildren(new ArrayList<>());
        }
        return mindMapNode;
    }
}
