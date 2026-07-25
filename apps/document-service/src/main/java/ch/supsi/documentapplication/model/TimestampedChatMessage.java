package ch.supsi.documentapplication.model;

import java.time.Instant;
import org.springframework.ai.chat.messages.Message;

public record TimestampedChatMessage(Message message, Instant timestamp) {}
