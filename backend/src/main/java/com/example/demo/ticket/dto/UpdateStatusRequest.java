
package com.example.demo.ticket.dto;

import com.example.demo.ticket.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
    @NotNull TicketStatus status
) {}
