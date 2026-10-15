
package com.example.demo.ticket.dto;

import com.example.demo.ticket.Ticket;
import com.example.demo.ticket.TicketPriority;
import com.example.demo.ticket.TicketStatus;

import java.time.Instant;

public record TicketResponse(
    Long id,
    String subject,
    String description,
    TicketPriority priority,
    TicketStatus status,
    Long organizationId,
    Long requesterId,
    String requesterName,
    Long assigneeId,
    String assigneeName,
    Instant createdAt,
    Instant updatedAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
            ticket.getId(),
            ticket.getSubject(),
            ticket.getDescription(),
            ticket.getPriority(),
            ticket.getStatus(),
            ticket.getOrganization().getId(),
            ticket.getRequester().getId(),
            ticket.getRequester().getName(),
            ticket.getAssignee() == null
                ? null : ticket.getAssignee().getId(),
            ticket.getAssignee() == null
                ? null : ticket.getAssignee().getName(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt()
        );
    }
}
