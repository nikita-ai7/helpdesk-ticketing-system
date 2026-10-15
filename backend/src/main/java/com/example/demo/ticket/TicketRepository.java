package com.example.demo.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByIdAndOrganization_Id(
            Long id,
            Long organizationId
    );

   
@Query("""
 SELECT t FROM Ticket t
 WHERE t.organization.id = :organizationId
 AND (:requesterId IS NULL OR t.requester.id = :requesterId)
 AND (:status IS NULL OR t.status = :status)
 AND (:priority IS NULL OR t.priority = :priority)
AND (
    LOWER(t.subject) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))
)
 """)
Page<Ticket> searchTickets(
    @Param("organizationId") Long organizationId,
    @Param("requesterId") Long requesterId,
    @Param("status") TicketStatus status,
    @Param("priority") TicketPriority priority,
    @Param("search") String search,
    Pageable pageable
    );
}