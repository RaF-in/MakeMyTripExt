package com.mmtext.bookingservice.controller;

import com.mmtext.bookingservice.dto.*;
import com.mmtext.bookingservice.enums.Enums;
import com.mmtext.bookingservice.service.BookingService;
import com.mmtext.bookingservice.service.QueueStatusService;
import com.mmtext.bookingservice.service.RateLimiterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Add this import at the top of BookingController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private QueueStatusService queueStatusService;


    // Add this field to BookingController class
    @Autowired
    private RateLimiterService rateLimiterService;

    /**
     * Create a new booking
     * POST /api/v1/bookings
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        logger.info("Received booking request for user: {}, ticket: {}",
                request.getUserId(), request.getTicketId());

        try {
            BookingResponse response = bookingService.createBooking(request);

            if (response.getStatus() == Enums.BookingStatus.CANCELLED) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid booking request: {}", e.getMessage());
            BookingResponse errorResponse = new BookingResponse();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Error creating booking", e);
            BookingResponse errorResponse = new BookingResponse();
            errorResponse.setMessage("An error occurred while processing your booking");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get booking details
     * GET /api/v1/bookings/{bookingReference}
     */
    @GetMapping("/{bookingReference}")
    public ResponseEntity<BookingDetailsResponse> getBookingDetails(
            @PathVariable String bookingReference) {

        logger.info("Fetching booking details for: {}", bookingReference);

        try {
            BookingDetailsResponse response = bookingService.getBookingDetails(bookingReference);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Booking not found: {}", bookingReference);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error fetching booking details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cancel a booking
     * POST /api/v1/bookings/cancel
     */
    @PostMapping("/cancel")
    public ResponseEntity<CancelBookingResponse> cancelBooking(
            @RequestBody CancelBookingRequest request) {

        logger.info("Received cancellation request for booking: {}", request.getBookingReference());

        try {
            CancelBookingResponse response = bookingService.cancelBooking(request);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid cancellation request: {}", e.getMessage());
            CancelBookingResponse errorResponse = new CancelBookingResponse(
                    request.getBookingReference(),
                    null,
                    e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Error cancelling booking", e);
            CancelBookingResponse errorResponse = new CancelBookingResponse(
                    request.getBookingReference(),
                    null,
                    "An error occurred while cancelling your booking"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Payment webhook endpoint
     * POST /api/v1/bookings/webhook/payment
     */
    @PostMapping("/webhook/payment")
    public ResponseEntity<String> handlePaymentWebhook(@RequestBody PaymentWebhookRequest request) {
        logger.info("Received payment webhook for booking: {}, Status: {}",
                request.getBookingReference(), request.getStatus());

        try {
            bookingService.handlePaymentWebhook(request);
            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            logger.error("Error processing payment webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook");
        }
    }

    /**
     * Health check endpoint
     * GET /api/v1/bookings/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Booking service is healthy");
    }

    /**
     * SSE endpoint for queue status updates with rate limiting
     * GET /api/v1/bookings/{bookingReference}/queue-status
     */
    @GetMapping(value = "/{bookingReference}/queue-status", produces = "text/event-stream")
    public SseEmitter getQueueStatus(@PathVariable String bookingReference) {
        logger.info("SSE connection request for booking: {}", bookingReference);

        // Rate limiting check
        if (!rateLimiterService.allowConnection(bookingReference)) {
            logger.warn("Rate limit exceeded for booking: {}", bookingReference);

            SseEmitter emitter = new SseEmitter(1000L);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"error\":\"Too many connection attempts. Please wait and try again.\"}")
                        .reconnectTime(60000)); // Tell client to wait 60 seconds
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
            return emitter;
        }

        try {
            // Verify booking exists and is in appropriate status
            BookingDetailsResponse booking = bookingService.getBookingDetails(bookingReference);

            if (booking.getStatus() != Enums.BookingStatus.QUEUED &&
                    booking.getStatus() != Enums.BookingStatus.PAYMENT_PENDING) {

                // If not in queue, return immediate response and close
                SseEmitter emitter = new SseEmitter(1000L);
                emitter.send(SseEmitter.event()
                        .name("queue-status")
                        .data("{\"status\":\"" + booking.getStatus() + "\",\"message\":\"Booking not in queue\"}"));
                emitter.complete();
                return emitter;
            }

            return queueStatusService.createQueueStatusStream(bookingReference);

        } catch (IllegalArgumentException | IOException e) {
            logger.error("Booking not found for SSE: {}", bookingReference);
            SseEmitter emitter = new SseEmitter(1000L);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"error\":\"Booking not found\"}"));
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
            return emitter;
        }
    }

    /**
     * Get active SSE connections count (for monitoring)
     * GET /api/v1/bookings/metrics/active-connections
     */
    @GetMapping("/metrics/active-connections")
    public ResponseEntity<Map<String, Integer>> getActiveConnections() {
        int count = queueStatusService.getActiveConnectionCount();
        Map<String, Integer> response = new HashMap<>();
        response.put("activeConnections", count);
        return ResponseEntity.ok(response);
    }
}