
package com.example.demo.ticket.dto;

import com.example.demo.ticket.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
    @NotBlank
    @Size(max = 200)
    String subject,

    @NotBlank
    @Size(max = 10000)
    String description,

    @NotNull
    TicketPriority priority
) {}
