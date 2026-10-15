
package com.example.demo.ticket;

import com.example.demo.organization.Organization;
import com.example.demo.organization.OrganizationRepository;
import com.example.demo.ticket.dto.AssignTicketRequest;
import com.example.demo.ticket.dto.UpdateStatusRequest;
import com.example.demo.user.AppUser;
import com.example.demo.user.AppUserRepository;
import com.example.demo.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private TicketHistoryRepository historyRepository;

    @InjectMocks
    private TicketService ticketService;

    private Organization acme;
    private Organization globex;
    private AppUser admin;
    private AppUser customer;
    private AppUser foreignAgent;
    private Ticket ticket;

    @BeforeEach
    void setUp() throws Exception {
        acme = organizationWithId(1L, "Acme", "acme");
        globex = organizationWithId(2L, "Globex", "globex");

        admin = userWithId(
                10L, "admin@acme.test", UserRole.ORG_ADMIN, acme);
        customer = userWithId(
                11L, "customer@acme.test", UserRole.CUSTOMER, acme);
        foreignAgent = userWithId(
                20L, "agent@globex.test", UserRole.AGENT, globex);

        ticket = new Ticket(
                "Cannot log in",
                "Login fails",
                TicketPriority.MEDIUM,
                acme,
                customer
        );
        setId(ticket, 100L);
    }

    @Test
    void getReturnsNotFoundForTicketFromAnotherOrganization() {
        authenticate(admin, "acme");
        when(ticketRepository.findByIdAndOrganization_Id(100L, 1L))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.get("acme|admin@acme.test", 100L)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void customerCannotAccessAnotherCustomersTicket() throws Exception {
        AppUser anotherCustomer = userWithId(
                13L, "another@acme.test", UserRole.CUSTOMER, acme);

        Ticket otherTicket = new Ticket(
                "Private issue",
                "Details",
                TicketPriority.LOW,
                acme,
                anotherCustomer
        );
        setId(otherTicket, 101L);

        authenticate(customer, "acme");
        when(ticketRepository.findByIdAndOrganization_Id(101L, 1L))
                .thenReturn(Optional.of(otherTicket));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.get("acme|customer@acme.test", 101L)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void customerCannotChangeTicketStatus() {
        authenticate(customer, "acme");
        when(ticketRepository.findByIdAndOrganization_Id(100L, 1L))
                .thenReturn(Optional.of(ticket));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.updateStatus(
                        "acme|customer@acme.test",
                        100L,
                        new UpdateStatusRequest(TicketStatus.RESOLVED))
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(historyRepository, never()).save(any(TicketHistory.class));
    }

    @Test
    void customerCannotAssignTicket() {
        authenticate(customer, "acme");
        when(ticketRepository.findByIdAndOrganization_Id(100L, 1L))
                .thenReturn(Optional.of(ticket));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.assign(
                        "acme|customer@acme.test",
                        100L,
                        new AssignTicketRequest(12L))
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void closedTicketCannotBeReopened() {
        authenticate(admin, "acme");
        ticket.setStatus(TicketStatus.CLOSED);

        when(ticketRepository.findByIdAndOrganization_Id(100L, 1L))
                .thenReturn(Optional.of(ticket));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.updateStatus(
                        "acme|admin@acme.test",
                        100L,
                        new UpdateStatusRequest(TicketStatus.OPEN))
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(historyRepository, never()).save(any(TicketHistory.class));
    }

    @Test
    void validStatusChangeSavesTicketAndHistory() {
        authenticate(admin, "acme");
        when(ticketRepository.findByIdAndOrganization_Id(100L, 1L))
                .thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ticketService.updateStatus(
                "acme|admin@acme.test",
                100L,
                new UpdateStatusRequest(TicketStatus.IN_PROGRESS));

        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
        verify(ticketRepository).save(ticket);

        ArgumentCaptor<TicketHistory> captor =
                ArgumentCaptor.forClass(TicketHistory.class);
        verify(historyRepository).save(captor.capture());

        TicketHistory history = captor.getValue();
        assertEquals("STATUS_CHANGED", history.getAction());
        assertEquals("OPEN", history.getOldValue());
        assertEquals("IN_PROGRESS", history.getNewValue());
    }

    @Test
    void assignmentRejectsAgentFromAnotherOrganization() {
        authenticate(admin, "acme");
        when(ticketRepository.findByIdAndOrganization_Id(100L, 1L))
                .thenReturn(Optional.of(ticket));
        when(userRepository.findById(20L))
                .thenReturn(Optional.of(foreignAgent));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> ticketService.assign(
                        "acme|admin@acme.test",
                        100L,
                        new AssignTicketRequest(20L))
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(historyRepository, never()).save(any(TicketHistory.class));
    }

    private void authenticate(AppUser user, String slug) {
        when(organizationRepository.findBySlug(slug))
                .thenReturn(Optional.of(user.getOrganization()));

        when(userRepository.findByEmailIgnoreCaseAndOrganization_Id(
                user.getEmail(), user.getOrganization().getId()))
                .thenReturn(Optional.of(user));
    }

    private static Organization organizationWithId(
            Long id, String name, String slug) throws Exception {
        Organization organization = new Organization(name, slug);
        setId(organization, id);
        return organization;
    }

    private static AppUser userWithId(
            Long id,
            String email,
            UserRole role,
            Organization organization) throws Exception {
        AppUser user = new AppUser(
                email, email, "test-password", role, organization);
        setId(user, id);
        return user;
    }

    private static void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}
