package ch.supsi.documentapplication.model;

import java.time.Instant;
import java.util.UUID;

public record Chat(UUID id, Long spaceId, String owner, String title, Instant createdAt, Instant updatedAt) {

    public static final String DEFAULT_TITLE = "New chat";
}
