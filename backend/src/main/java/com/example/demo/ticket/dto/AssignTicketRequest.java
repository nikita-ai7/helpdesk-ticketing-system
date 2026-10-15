
package com.example.demo.ticket.dto;

import jakarta.validation.constraints.NotNull;

public record AssignTicketRequest(
    @NotNull Long assigneeId
) {}
