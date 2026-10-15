
package com.example.demo.comment;

import com.example.demo.comment.dto.CommentResponse;
import com.example.demo.comment.dto.CreateCommentRequest;
import com.example.demo.organization.Organization;
import com.example.demo.organization.OrganizationRepository;
import com.example.demo.ticket.*;
import com.example.demo.user.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TicketCommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final AppUserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public TicketCommentService(
            TicketCommentRepository commentRepository,
            TicketRepository ticketRepository,
            AppUserRepository userRepository,
            OrganizationRepository organizationRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

private AppUser currentUser(String principal) {
    String[] parts = principal.split("\\|", 2);

    if (parts.length != 2) {
        throw new org.springframework.security.authentication.BadCredentialsException(
                "Invalid authenticated username format");
    }

    String organizationSlug = parts[0];
    String email = parts[1];

    Organization organization = organizationRepository
            .findBySlug(organizationSlug)
            .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException(
                    "Invalid credentials"));
return userRepository
        .findByEmailIgnoreCaseAndOrganization_Id(
                email, organization.getId())
            .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException(
                    "Invalid credentials"));
}

    private Ticket accessibleTicket(Long ticketId, AppUser user) {
        Ticket ticket = ticketRepository
            .findByIdAndOrganization_Id(
                ticketId, user.getOrganization().getId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Ticket not found"));

        if (user.getRole() == UserRole.CUSTOMER
                && !ticket.getRequester().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Ticket not found");
        }

        return ticket;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(String principal, Long ticketId) {
        AppUser user = currentUser(principal);
        accessibleTicket(ticketId, user);

        List<TicketComment> comments =
            user.getRole() == UserRole.CUSTOMER
                ? commentRepository
                    .findByTicket_IdAndInternalNoteFalseOrderByCreatedAtAsc(
                        ticketId)
                : commentRepository
                    .findByTicket_IdOrderByCreatedAtAsc(ticketId);

        return comments.stream()
            .map(CommentResponse::from)
            .toList();
    }

    @Transactional
    public CommentResponse create(
            String principal,
            Long ticketId,
            CreateCommentRequest request) {
        AppUser user = currentUser(principal);
        Ticket ticket = accessibleTicket(ticketId, user);

        if (user.getRole() == UserRole.CUSTOMER
                && request.internalNote()) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Customers cannot create internal notes");
        }

        TicketComment comment = new TicketComment(
            ticket,
            user,
            request.body().trim(),
            request.internalNote()
        );

        return CommentResponse.from(commentRepository.save(comment));
    }
}
