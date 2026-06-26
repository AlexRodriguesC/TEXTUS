package ch.supsi.documentapplication.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class FsDocument {

    private final UUID documentId;
    private final byte[] bytes;

    private FsDocument(UUID documentId, byte[] bytes) {
        this.documentId = documentId;
        this.bytes = bytes;
    }

    public static FsDocument create(byte[] bytes) {
        return new FsDocument(UUID.randomUUID(), bytes);
    }

    public static FsDocument create(UUID uuid, byte[] bytes) {
        return new FsDocument(uuid, bytes);
    }
}
