package sse.sse.model;

import java.time.LocalDateTime;

public record Message(
        String message,
        String sender,
        LocalDateTime createdAt
) {
}
