package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChunkDeserializer extends StdDeserializer<Chunk> {

    public ChunkDeserializer() {
        super(Chunk.class);
    }

    @Override
    public Chunk deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode root = codec.readTree(jsonParser);

        Chunk chunk = new Chunk();

        chunk.setTitle(getText(root, "title"));
        chunk.setSummary(getText(root, "summary"));
        chunk.setKey(getText(root, "key"));

        chunk.setKeywords(getStringList(root, "keywords"));
        chunk.setQuestions(getStringList(root, "questions"));
        chunk.setRelatedKeys(getStringList(root, "related_keys"));

        chunk.setEntities(getEntities(root));

        return chunk;
    }

    private String getText(JsonNode root, String field) throws JsonParseException {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            throw new JsonParseException("Expected text field '%s'".formatted(field));
        }
        if (!node.isTextual()) {
            throw new JsonParseException(
                    null, "Expected string for field '%s' but got: %s".formatted(field, node.getNodeType()));
        }
        return node.asText();
    }

    private List<String> getStringList(JsonNode root, String field) throws JsonParseException {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            throw new JsonParseException("Expected string list field '%s'".formatted(field));
        }
        if (!node.isArray()) {
            throw new JsonParseException(
                    null, "Expected array for field '%s' but got: %s".formatted(field, node.getNodeType()));
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < node.size(); i++) {
            JsonNode item = node.get(i);
            if (item.isNull()) continue;
            if (!item.isTextual()) {
                throw new JsonParseException(
                        null, "Expected string at '%s[%d]' but got: %s".formatted(field, i, item.getNodeType()));
            }
            result.add(item.asText());
        }
        return result;
    }

    private List<Entity> getEntities(JsonNode root) throws JsonParseException {
        JsonNode node = root.get("entities");
        if (node == null || node.isNull()) {
            throw new JsonParseException("Expected entities list field '%s'".formatted("entities"));
        }
        if (!node.isArray()) {
            throw new JsonParseException(
                    null, "Expected array for field '%s' but got: %s".formatted("entities", node.getNodeType()));
        }
        List<Entity> result = new ArrayList<>();
        for (int i = 0; i < node.size(); i++) {
            JsonNode item = node.get(i);
            if (item.isNull()) continue;
            if (!item.isObject()) {
                throw new JsonParseException(
                        null, "Expected object at '%s[%d]' but got: %s".formatted("entities", i, item.getNodeType()));
            }
            String name = getRequiredText(item, "name", "entities" + "[" + i + "]");
            String type = getRequiredText(item, "type", "entities" + "[" + i + "]");
            result.add(new Entity(name, type).validate());
        }
        return result;
    }

    private String getRequiredText(JsonNode parent, String field, String context) throws JsonParseException {
        JsonNode node = parent.get(field);
        if (node == null || node.isNull()) {
            throw new JsonParseException(null, "Missing required field '%s' in %s".formatted(field, context));
        }
        if (!node.isTextual()) {
            throw new JsonParseException(
                    null,
                    "Expected string for field '%s' in %s but got: %s".formatted(field, context, node.getNodeType()));
        }
        return node.asText();
    }
}
