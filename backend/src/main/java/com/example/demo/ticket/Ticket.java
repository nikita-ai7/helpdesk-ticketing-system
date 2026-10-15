
package com.example.demo.ticket;

import com.example.demo.organization.Organization;
import com.example.demo.user.AppUser;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_ticket_org_status", columnList = "organization_id,status"),
    @Index(name = "idx_ticket_org_created", columnList = "organization_id,created_at")
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, length = 10000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority = TicketPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private AppUser requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private AppUser assignee;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected Ticket() {
    }

    public Ticket(
            String subject,
            String description,
            TicketPriority priority,
            Organization organization,
            AppUser requester) {
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.organization = organization;
        this.requester = requester;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public TicketPriority getPriority() { return priority; }
    public TicketStatus getStatus() { return status; }
    public Organization getOrganization() { return organization; }
    public AppUser getRequester() { return requester; }
    public AppUser getAssignee() { return assignee; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setSubject(String subject) { this.subject = subject; }
    public void setDescription(String description) { this.description = description; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public void setAssignee(AppUser assignee) { this.assignee = assignee; }
}
