
package com.example.demo.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketCommentRepository
        extends JpaRepository<TicketComment, Long> {

    List<TicketComment> findByTicket_IdOrderByCreatedAtAsc(Long ticketId);

    List<TicketComment> findByTicket_IdAndInternalNoteFalseOrderByCreatedAtAsc(
            Long ticketId);
}
