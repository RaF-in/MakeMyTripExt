package com.mmtext.bookingservice.controller;

import com.mmtext.bookingservice.dto.AvailableTicketDTO;
import com.mmtext.bookingservice.dto.TicketAvailabilityResponse;
import com.mmtext.bookingservice.service.TicketAvailabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketAvailabilityService ticketAvailabilityService;

    /**
     * Get available tickets for an event
     * Scans both DB and Redis to return only truly available tickets
     * GET /api/v1/tickets/available/{eventId}
     */
    @GetMapping("/available/{eventId}")
    public ResponseEntity<List<AvailableTicketDTO>> getAvailableTickets(
            @PathVariable String eventId) {

        logger.info("Fetching available tickets for event: {}", eventId);

        try {
            List<AvailableTicketDTO> availableTickets =
                    ticketAvailabilityService.getAvailableTickets(eventId);

            return ResponseEntity.ok(availableTickets);

        } catch (Exception e) {
            logger.error("Error fetching available tickets for event: {}", eventId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if a specific ticket is available
     * GET /api/v1/tickets/check/{ticketId}
     */
    @GetMapping("/check/{ticketId}")
    public ResponseEntity<TicketAvailabilityResponse> checkTicketAvailability(
            @PathVariable String ticketId) {

        logger.info("Checking availability for ticket: {}", ticketId);

        try {
            boolean available = ticketAvailabilityService.isTicketAvailable(ticketId);

            TicketAvailabilityResponse response = new TicketAvailabilityResponse(
                    ticketId,
                    available,
                    available ? "Ticket is available" : "Ticket is not available"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error checking ticket availability: {}", ticketId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

