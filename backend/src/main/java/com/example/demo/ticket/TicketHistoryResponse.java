package com.example.demo.ticket;

import java.time.Instant;

public record TicketHistoryResponse(
        Long id,
        String action,
        String oldValue,
        String newValue,
        Long changedById,
        String changedByName,
        Instant createdAt
) {
    public static TicketHistoryResponse from(TicketHistory history) {
        return new TicketHistoryResponse(
                history.getId(),
                history.getAction(),
                history.getOldValue(),
                history.getNewValue(),
                history.getChangedBy().getId(),
                history.getChangedBy().getName(),
                history.getCreatedAt()
        );
    }
}