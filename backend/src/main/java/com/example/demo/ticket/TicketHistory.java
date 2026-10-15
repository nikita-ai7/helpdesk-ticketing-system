
package com.example.demo.ticket;

import com.example.demo.user.AppUser;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ticket_history", indexes = {
    @Index(name = "idx_history_ticket_created", columnList = "ticket_id,created_at")
})
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by_id", nullable = false)
    private AppUser changedBy;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 100)
    private String oldValue;

    @Column(length = 100)
    private String newValue;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected TicketHistory() {
    }

    public TicketHistory(
            Ticket ticket,
            AppUser changedBy,
            String action,
            String oldValue,
            String newValue) {
        this.ticket = ticket;
        this.changedBy = changedBy;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Long getId() { return id; }
    public Ticket getTicket() { return ticket; }
    public AppUser getChangedBy() { return changedBy; }
    public String getAction() { return action; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public Instant getCreatedAt() { return createdAt; }
}
