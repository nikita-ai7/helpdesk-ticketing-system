
package com.example.demo.ticket;

import com.example.demo.organization.Organization;
import com.example.demo.organization.OrganizationRepository;
import com.example.demo.ticket.dto.CreateTicketRequest;
import com.example.demo.ticket.dto.TicketResponse;
import com.example.demo.ticket.dto.UpdateStatusRequest;
import com.example.demo.ticket.dto.AssignTicketRequest;
import com.example.demo.user.AppUser;
import com.example.demo.user.AppUserRepository;
import com.example.demo.user.UserRole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AppUserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final TicketHistoryRepository historyRepository;

    public TicketService(
            TicketRepository ticketRepository,
            AppUserRepository userRepository,
            OrganizationRepository organizationRepository,
            TicketHistoryRepository historyRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.historyRepository = historyRepository;
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

    private Ticket scopedTicket(Long ticketId, AppUser user) {
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

    @Transactional
    public TicketResponse create(
            String principal, CreateTicketRequest request) {
        AppUser user = currentUser(principal);

        Ticket ticket = new Ticket(
            request.subject().trim(),
            request.description().trim(),
            request.priority(),
            user.getOrganization(),
            user
        );

        ticket = ticketRepository.save(ticket);

        historyRepository.save(new TicketHistory(
            ticket, user, "CREATED", null, "OPEN"
        ));

        return TicketResponse.from(ticket);
    }

    @Transactional(readOnly = true)
public Page<TicketResponse> list(
        String principal,
        TicketStatus status,
        TicketPriority priority,
        String search,
        Pageable pageable
) {
    AppUser user = currentUser(principal);

    // Customers can only see their own tickets.
    Long requesterId = user.getRole() == UserRole.CUSTOMER
            ? user.getId()
            : null;

  String normalizedSearch = (search == null || search.isBlank()) ? "" : search.trim();

    return ticketRepository.searchTickets(
            user.getOrganization().getId(),
            requesterId,
            status,
            priority,
            normalizedSearch,
            pageable
    ).map(TicketResponse::from);
}

    @Transactional(readOnly = true)
    public TicketResponse get(String principal, Long ticketId) {
        AppUser user = currentUser(principal);
        return TicketResponse.from(scopedTicket(ticketId, user));
    }

    @Transactional
    public TicketResponse updateStatus(
            String principal,
            Long ticketId,
            UpdateStatusRequest request) {

        AppUser user = currentUser(principal);
        Ticket ticket = scopedTicket(ticketId, user);

        if (user.getRole() == UserRole.CUSTOMER) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Customers cannot change ticket status");
        }

        TicketStatus previous = ticket.getStatus();
        TicketStatus next = request.status();

        if (previous == TicketStatus.CLOSED
                && next != TicketStatus.CLOSED) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Closed tickets cannot be reopened in this version");
        }

        if (previous != next) {
            ticket.setStatus(next);
            ticketRepository.save(ticket);

            historyRepository.save(new TicketHistory(
                ticket, user, "STATUS_CHANGED",
                previous.name(), next.name()
            ));
        }

        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse assign(
            String principal,
            Long ticketId,
            AssignTicketRequest request) {

        AppUser user = currentUser(principal);
        Ticket ticket = scopedTicket(ticketId, user);

        if (user.getRole() == UserRole.CUSTOMER) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Customers cannot assign tickets");
        }

        AppUser assignee = userRepository
            .findById(request.assigneeId())
            .filter(candidate ->
                candidate.getOrganization().getId()
                    .equals(user.getOrganization().getId()))
            .filter(candidate -> candidate.getRole() == UserRole.AGENT)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Assignee must be an agent in your organization"));

        AppUser previous = ticket.getAssignee();

        if (previous == null
                || !previous.getId().equals(assignee.getId())) {
            ticket.setAssignee(assignee);
            ticketRepository.save(ticket);

            historyRepository.save(new TicketHistory(
                ticket,
                user,
                "ASSIGNED",
                previous == null ? null : previous.getId().toString(),
                assignee.getId().toString()
            ));
        }

        return TicketResponse.from(ticket);
    }
}
