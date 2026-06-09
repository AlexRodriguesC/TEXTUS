package ch.supsi.textusbackend.model;

import java.util.UUID;
import lombok.Getter;

@Getter
public class Document {

    private final UUID documentId;
    private final byte[] bytes;

    private Document(UUID documentId, byte[] bytes) {
        this.documentId = documentId;
        this.bytes = bytes;
    }

    public static Document create(byte[] bytes) {
        return new Document(UUID.randomUUID(), bytes);
    }

    public static Document create(UUID uuid, byte[] bytes) {
        return new Document(uuid, bytes);
    }
}
