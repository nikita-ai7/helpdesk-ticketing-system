package com.example.demo.ticket;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/history")
public class TicketHistoryController {

    private final TicketHistoryService historyService;

    public TicketHistoryController(TicketHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public List<TicketHistoryResponse> list(
            Authentication authentication,
            @PathVariable Long ticketId) {

        return historyService
                .list(authentication.getName(), ticketId)
                .stream()
                .map(TicketHistoryResponse::from)
                .toList();
    }
}