package com.mmtext.bookingservice.controller;

import com.mmtext.bookingservice.dto.AddTicketRequest;
import com.mmtext.bookingservice.dto.BulkAddTicketRequest;
import com.mmtext.bookingservice.dto.BulkTicketResponse;
import com.mmtext.bookingservice.dto.TicketResponse;
import com.mmtext.bookingservice.enums.Enums;
import com.mmtext.bookingservice.model.Ticket;
import com.mmtext.bookingservice.repo.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/tickets")
public class AdminTicketController {

    private static final Logger logger = LoggerFactory.getLogger(AdminTicketController.class);

    @Autowired
    private TicketRepository ticketRepository;

    /**
     * Add a single ticket
     * POST /api/v1/admin/tickets
     */
    @PostMapping
    public ResponseEntity<TicketResponse> addTicket(@RequestBody AddTicketRequest request) {
        logger.info("Admin adding ticket for event: {}", request.getEventId());

        try {
            String ticketId = request.getEventId() + ":" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            Ticket ticket = new Ticket(
                    ticketId,
                    request.getEventId(),
                    request.getEventName(),
                    request.getConcurrencyType(),
                    request.getPrice(),
                    request.getSeatNumber()
            );

            ticketRepository.save(ticket);

            logger.info("Ticket created: {}", ticketId);

            TicketResponse response = new TicketResponse(
                    ticket.getTicketId(),
                    ticket.getEventId(),
                    ticket.getEventName(),
                    ticket.getSeatNumber(),
                    ticket.getPrice(),
                    ticket.getConcurrencyType(),
                    ticket.getStatus()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating ticket", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add multiple tickets in bulk
     * POST /api/v1/admin/tickets/bulk
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkTicketResponse> addTicketsBulk(@RequestBody BulkAddTicketRequest request) {
        logger.info("Admin adding {} tickets for event: {}", request.getQuantity(), request.getEventId());

        try {
            List<Ticket> tickets = new ArrayList<>();
            List<TicketResponse> createdTickets = new ArrayList<>();

            for (int i = 0; i < request.getQuantity(); i++) {
                String ticketId = request.getEventId() + ":" +
                        UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                String seatNumber = generateSeatNumber(request.getSeatPrefix(), i + 1);

                Ticket ticket = new Ticket(
                        ticketId,
                        request.getEventId(),
                        request.getEventName(),
                        request.getConcurrencyType(),
                        request.getPrice(),
                        seatNumber
                );

                tickets.add(ticket);

                createdTickets.add(new TicketResponse(
                        ticket.getTicketId(),
                        ticket.getEventId(),
                        ticket.getEventName(),
                        ticket.getSeatNumber(),
                        ticket.getPrice(),
                        ticket.getConcurrencyType(),
                        ticket.getStatus()
                ));
            }

            // Batch save for performance
            ticketRepository.saveAll(tickets);

            logger.info("Created {} tickets for event: {}", tickets.size(), request.getEventId());

            BulkTicketResponse response = new BulkTicketResponse(
                    request.getEventId(),
                    createdTickets.size(),
                    createdTickets
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating tickets in bulk", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all tickets for an event (admin view)
     * GET /api/v1/admin/tickets/event/{eventId}
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<TicketResponse>> getEventTickets(@PathVariable String eventId) {
        logger.info("Admin fetching all tickets for event: {}", eventId);

        try {
            List<Ticket> tickets = ticketRepository.findByEventId(eventId);

            List<TicketResponse> response = tickets.stream()
                    .map(ticket -> new TicketResponse(
                            ticket.getTicketId(),
                            ticket.getEventId(),
                            ticket.getEventName(),
                            ticket.getSeatNumber(),
                            ticket.getPrice(),
                            ticket.getConcurrencyType(),
                            ticket.getStatus()
                    ))
                    .toList();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching tickets", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a ticket
     * DELETE /api/v1/admin/tickets/{ticketId}
     */
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<String> deleteTicket(@PathVariable String ticketId) {
        logger.info("Admin deleting ticket: {}", ticketId);

        try {
            Ticket ticket = ticketRepository.findByTicketId(ticketId)
                    .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

            if (ticket.getStatus() == Enums.TicketStatus.BOOKED) {
                return ResponseEntity.badRequest()
                        .body("Cannot delete booked ticket");
            }

            ticketRepository.delete(ticket);

            logger.info("Ticket deleted: {}", ticketId);

            return ResponseEntity.ok("Ticket deleted successfully");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting ticket", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate seat number based on prefix and index
     */
    private String generateSeatNumber(String prefix, int index) {
        if (prefix == null || prefix.isEmpty()) {
            prefix = "SEAT";
        }

        // Format: A-1, A-2, ... or SEAT-001, SEAT-002, etc.
        if (prefix.length() == 1) {
            return String.format("%s-%d", prefix, index);
        } else {
            return String.format("%s-%03d", prefix, index);
        }
    }
}






