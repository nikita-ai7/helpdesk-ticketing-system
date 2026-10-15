
package com.example.demo.comment;

import com.example.demo.comment.dto.CommentResponse;
import com.example.demo.comment.dto.CreateCommentRequest;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
public class TicketCommentController {

    private final TicketCommentService commentService;

    public TicketCommentController(TicketCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentResponse> list(
            Authentication authentication,
            @PathVariable Long ticketId) {
        return commentService.list(
            authentication.getName(), ticketId);
    }

    @PostMapping
    public CommentResponse create(
            Authentication authentication,
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateCommentRequest request) {
        return commentService.create(
            authentication.getName(), ticketId, request);
    }
}
