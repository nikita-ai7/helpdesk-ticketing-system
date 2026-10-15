
package com.example.demo.ticket;

import com.example.demo.organization.Organization;
import com.example.demo.organization.OrganizationRepository;
import com.example.demo.user.AppUser;
import com.example.demo.user.AppUserRepository;
import com.example.demo.user.UserRole;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TicketHistoryService {

    private final TicketHistoryRepository historyRepository;
    private final TicketRepository ticketRepository;
    private final AppUserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public TicketHistoryService(
            TicketHistoryRepository historyRepository,
            TicketRepository ticketRepository,
            AppUserRepository userRepository,
            OrganizationRepository organizationRepository) {
        this.historyRepository = historyRepository;
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

    @Transactional(readOnly = true)
    public List<TicketHistory> list(String principal, Long ticketId) {
        AppUser user = currentUser(principal);

        if (user.getRole() == UserRole.CUSTOMER) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Ticket history is restricted to organization staff");
        }

        ticketRepository.findByIdAndOrganization_Id(
            ticketId, user.getOrganization().getId()
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Ticket not found"));

        return historyRepository
            .findByTicket_IdOrderByCreatedAtAsc(ticketId);
    }
}
