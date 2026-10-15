
package com.example.demo.comment;

import com.example.demo.ticket.Ticket;
import com.example.demo.user.AppUser;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ticket_comments", indexes = {
    @Index(name = "idx_comment_ticket_created", columnList = "ticket_id,created_at")
})
public class TicketComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser author;

    @Column(nullable = false, length = 10000)
    private String body;

    @Column(nullable = false)
    private boolean internalNote = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected TicketComment() {
    }

    public TicketComment(
            Ticket ticket,
            AppUser author,
            String body,
            boolean internalNote) {
        this.ticket = ticket;
        this.author = author;
        this.body = body;
        this.internalNote = internalNote;
    }

    public Long getId() { return id; }
    public Ticket getTicket() { return ticket; }
    public AppUser getAuthor() { return author; }
    public String getBody() { return body; }
    public boolean isInternalNote() { return internalNote; }
    public Instant getCreatedAt() { return createdAt; }
}
