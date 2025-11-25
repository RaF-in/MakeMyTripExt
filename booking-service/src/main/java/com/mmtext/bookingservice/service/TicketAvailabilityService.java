package com.mmtext.bookingservice.service;

import com.mmtext.bookingservice.dto.AvailableTicketDTO;
import com.mmtext.bookingservice.model.Ticket;
import com.mmtext.bookingservice.model.TicketStatus;
import com.mmtext.bookingservice.repo.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TicketAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(TicketAvailabilityService.class);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RedisService redisService;

    /**
     * Get available tickets for an event
     * Filters out tickets that are:
     * 1. Already BOOKED in DB
     * 2. Locked in Redis (currently in payment process)
     */
    public List<AvailableTicketDTO> getAvailableTickets(String eventId) {
        logger.info("Fetching available tickets for event: {}", eventId);

        // Step 1: Get all tickets with status AVAILABLE from DB
        List<Ticket> availableInDb = ticketRepository.findByEventIdAndStatus(
                eventId,
                TicketStatus.AVAILABLE
        );

        // Step 2: Get locked ticket IDs from Redis
        Set<String> lockedTicketIds = redisService.getLockedTicketIds(eventId);

        logger.info("Event {}: {} tickets in DB, {} locked in Redis",
                eventId, availableInDb.size(), lockedTicketIds.size());

        // Step 3: Filter out locked tickets
        List<AvailableTicketDTO> availableTickets = availableInDb.stream()
                .filter(ticket -> !lockedTicketIds.contains(ticket.getTicketId()))
                .map(ticket -> new AvailableTicketDTO(
                        ticket.getTicketId(),
                        ticket.getEventId(),
                        ticket.getEventName(),
                        ticket.getSeatNumber(),
                        ticket.getPrice(),
                        ticket.getConcurrencyType()
                ))
                .collect(Collectors.toList());

        logger.info("Event {}: {} tickets actually available", eventId, availableTickets.size());

        return availableTickets;
    }

    /**
     * Check if a specific ticket is available
     */
    public boolean isTicketAvailable(String ticketId) {
        // Check DB first
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElse(null);

        if (ticket == null || ticket.getStatus() == TicketStatus.BOOKED) {
            return false;
        }

        // Check if locked in Redis
        return !redisService.isTicketLocked(ticketId);
    }

    /**
     * Get ticket details
     */
    public Ticket getTicket(String ticketId) {
        return ticketRepository.findByTicketId(ticketId)
                .orElse(null);
    }
}