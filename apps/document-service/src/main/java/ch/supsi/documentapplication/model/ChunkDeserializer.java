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
        chunk.setRelatedKeys(getStringList(root, "related_keys"));

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
}
