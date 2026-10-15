
package com.example.demo.comment.dto;

import com.example.demo.comment.TicketComment;
import java.time.Instant;

public record CommentResponse(
    Long id,
    Long authorId,
    String authorName,
    String body,
    boolean internalNote,
    Instant createdAt
) {
    public static CommentResponse from(TicketComment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getAuthor().getId(),
            comment.getAuthor().getName(),
            comment.getBody(),
            comment.isInternalNote(),
            comment.getCreatedAt()
        );
    }
}
