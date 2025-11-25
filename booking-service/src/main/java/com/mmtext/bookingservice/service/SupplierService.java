package com.mmtext.bookingservice.service;

import com.mmtext.bookingservice.dto.SupplierReservationRequest;
import com.mmtext.bookingservice.dto.SupplierReservationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;

@Service
public class SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);
    private final Random random = new Random();

    /**
     * Reserve ticket from supplier (OTA)
     * This is a dummy implementation that simulates supplier API call
     */
    public SupplierReservationResponse reserveTicket(SupplierReservationRequest request) {
        logger.info("Calling supplier to reserve ticket: {}", request.getTicketId());

        // Simulate network delay
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 90% success rate for available tickets
        boolean available = random.nextInt(100) < 90;

        if (available) {
            String reservationId = "SUP-" + UUID.randomUUID().toString();
            Instant expiredAt = Instant.now().plus(15, ChronoUnit.MINUTES); // 15 minutes to complete payment

            logger.info("Ticket reserved successfully. ReservationId: {}, ExpiredAt: {}",
                    reservationId, expiredAt);

            return new SupplierReservationResponse(
                    true,
                    reservationId,
                    expiredAt,
                    "Ticket reserved successfully"
            );
        } else {
            logger.warn("Ticket not available: {}", request.getTicketId());
            return new SupplierReservationResponse(
                    false,
                    null,
                    null,
                    "Ticket is not available"
            );
        }
    }

    /**
     * Cancel reservation at supplier
     */
    public boolean cancelReservation(String reservationId) {
        logger.info("Calling supplier to cancel reservation: {}", reservationId);

        // Simulate network delay
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 95% success rate for cancellation
        boolean success = random.nextInt(100) < 95;

        if (success) {
            logger.info("Reservation cancelled successfully: {}", reservationId);
        } else {
            logger.error("Failed to cancel reservation: {}", reservationId);
        }

        return success;
    }

    /**
     * Confirm booking at supplier after payment
     */
    public boolean confirmBooking(String reservationId, String paymentId) {
        logger.info("Calling supplier to confirm booking. ReservationId: {}, PaymentId: {}",
                reservationId, paymentId);

        // Simulate network delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 98% success rate for confirmation
        boolean success = random.nextInt(100) < 98;

        if (success) {
            logger.info("Booking confirmed at supplier: {}", reservationId);
        } else {
            logger.error("Failed to confirm booking at supplier: {}", reservationId);
        }

        return success;
    }
}