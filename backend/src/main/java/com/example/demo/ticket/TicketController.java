
package com.example.demo.ticket;

import com.example.demo.ticket.dto.AssignTicketRequest;
import com.example.demo.ticket.dto.CreateTicketRequest;
import com.example.demo.ticket.dto.TicketResponse;
import com.example.demo.ticket.dto.UpdateStatusRequest;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public TicketResponse create(
            Authentication authentication,
            @Valid @RequestBody CreateTicketRequest request) {
        return ticketService.create(
            authentication.getName(), request);
    }

    @GetMapping
    public Page<TicketResponse> list(
            Authentication authentication,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt")
            Pageable pageable) {

        return ticketService.list(
            authentication.getName(),
            status, priority, search, pageable);
    }

    @GetMapping("/{ticketId}")
    public TicketResponse get(
            Authentication authentication,
            @PathVariable Long ticketId) {
        return ticketService.get(
            authentication.getName(), ticketId);
    }

    @PatchMapping("/{ticketId}/status")
    public TicketResponse updateStatus(
            Authentication authentication,
            @PathVariable Long ticketId,
            @Valid @RequestBody UpdateStatusRequest request) {
        return ticketService.updateStatus(
            authentication.getName(), ticketId, request);
    }

    @PatchMapping("/{ticketId}/assignee")
    public TicketResponse assign(
            Authentication authentication,
            @PathVariable Long ticketId,
            @Valid @RequestBody AssignTicketRequest request) {
        return ticketService.assign(
            authentication.getName(), ticketId, request);
    }
}
