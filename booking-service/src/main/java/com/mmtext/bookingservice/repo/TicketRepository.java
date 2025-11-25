package com.mmtext.bookingservice.repo;

import com.mmtext.bookingservice.enums.Enums;
import com.mmtext.bookingservice.model.Ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketId(String ticketId);

    List<Ticket> findByEventIdAndStatus(String eventId, Enums.TicketStatus status);

    List<Ticket> findByEventId(String eventId);

    @Query("SELECT t FROM Ticket t WHERE t.eventId = :eventId AND t.status = :status AND t.concurrencyType = :concurrencyType")
    List<Ticket> findAvailableTicketsByEventAndType(
            @Param("eventId") String eventId,
            @Param("status") Enums.TicketStatus status,
            @Param("concurrencyType") Enums.ConcurrencyType concurrencyType
    );

    List<Ticket> findByBookingReference(String bookingReference);

    List<Ticket> findByBookedByUserId(String userId);
}