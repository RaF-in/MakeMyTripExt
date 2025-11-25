package com.mmtext.bookingservice.service;

import com.mmtext.bookingservice.dto.BookingData;
import com.mmtext.bookingservice.dto.NotificationRequest;
import com.mmtext.bookingservice.dto.PaymentRequest;
import com.mmtext.bookingservice.dto.PaymentResponse;
import com.mmtext.bookingservice.enums.Enums;
import com.mmtext.bookingservice.model.Booking;
import com.mmtext.bookingservice.model.Ticket;
import com.mmtext.bookingservice.repo.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class QueueProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(QueueProcessorService.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private QueueStatusService queueStatusService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private TicketAvailabilityService ticketAvailabilityService;

    @Autowired
    private NotificationService notificationService;

    @Value("${booking.queue.batch-size:100}")
    private int batchSize;

    /**
     * Process queue in batches
     * Runs every 5 seconds
     */
    @Scheduled(fixedDelayString = "${booking.queue.processing-interval:5000}")
    public void processQueues() {
        logger.debug("Starting queue processing cycle...");

        try {
            // Get all active queues from Redis
            Set<String> activeQueueKeys = redisService.getActiveQueueKeys();

            if (activeQueueKeys.isEmpty()) {
                logger.debug("No active queues to process");
                return;
            }

            logger.info("Processing {} active queues", activeQueueKeys.size());

            for (String queueKey : activeQueueKeys) {
                // Extract ticket ID from queue key
                String ticketId = queueKey.replace("queue:", "");

                try {
                    processQueueForTicket(ticketId);
                } catch (Exception e) {
                    logger.error("Error processing queue for ticket: {}", ticketId, e);
                }
            }

        } catch (Exception e) {
            logger.error("Error in queue processing cycle", e);
        }
    }

    /**
     * Process queue for a specific ticket - Redis-first approach
     */
    private void processQueueForTicket(String ticketId) {
        Long queueSize = redisService.getQueueSize(ticketId);

        if (queueSize == null || queueSize == 0) {
            logger.debug("Queue empty for ticket: {}", ticketId);
            return;
        }

        logger.info("Processing queue for ticket: {}, Queue size: {}", ticketId, queueSize);

        // Check if ticket is still available
        if (!ticketAvailabilityService.isTicketAvailable(ticketId)) {
            logger.warn("Ticket no longer available: {}. Cancelling remaining queue.", ticketId);
            cancelRemainingQueue(ticketId);
            return;
        }

        // Pop batch from queue atomically
        Set<Object> batch = redisService.popBatchFromQueue(ticketId, batchSize);

        if (batch.isEmpty()) {
            logger.debug("No items popped from queue for ticket: {}", ticketId);
            return;
        }

        logger.info("Processing batch of {} bookings for ticket: {}", batch.size(), ticketId);

        List<String> successfulBookings = new ArrayList<>();
        List<String> failedBookings = new ArrayList<>();
        Instant expiredAt = Instant.now().plus(15, ChronoUnit.MINUTES);
        long ttlSeconds = Duration.between(Instant.now(), expiredAt).getSeconds();

        // Get ticket info
        Ticket ticket = ticketAvailabilityService.getTicket(ticketId);
        if (ticket == null) {
            logger.error("Ticket not found: {}", ticketId);
            return;
        }

        int processedCount = 0;
        boolean ticketBecameUnavailable = false;

        for (Object bookingRefObj : batch) {
            String bookingReference = bookingRefObj.toString();

            try {
                // Check if already processing
                if (redisService.isProcessing(bookingReference)) {
                    logger.warn("Booking already processing: {}", bookingReference);
                    continue;
                }

                // Mark as processing
                redisService.markAsProcessing(bookingReference, ttlSeconds);

                // Get booking from DB (only for queue tracking)
                Booking booking = bookingRepository.findByBookingReference(bookingReference)
                        .orElse(null);

                if (booking == null) {
                    logger.error("Booking not found: {}", bookingReference);
                    redisService.removeFromProcessing(bookingReference);
                    failedBookings.add(bookingReference);
                    continue;
                }

                if (booking.getStatus() != Enums.BookingStatus.QUEUED) {
                    logger.warn("Booking not in QUEUED status: {} - {}",
                            bookingReference, booking.getStatus());
                    redisService.removeFromProcessing(bookingReference);
                    continue;
                }

                // Check if ticket still available before locking
                if (ticketBecameUnavailable || !ticketAvailabilityService.isTicketAvailable(ticketId)) {
                    logger.warn("Ticket became unavailable during batch processing: {}", ticketId);
                    ticketBecameUnavailable = true;

                    // Cancel this booking
                    booking.setStatus(Enums.BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                    redisService.removeFromProcessing(bookingReference);
                    failedBookings.add(bookingReference);

                    // Notify user
                    sendFailureNotification(booking, ticket, "Ticket no longer available");
                    continue;
                }

                // Lock ticket in Redis atomically
                boolean locked = redisService.lockTicket(ticketId, bookingReference, ttlSeconds);

                if (!locked) {
                    logger.warn("Failed to lock ticket for booking: {}", bookingReference);
                    booking.setStatus(Enums.BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                    redisService.removeFromProcessing(bookingReference);
                    failedBookings.add(bookingReference);

                    // Notify user
                    sendFailureNotification(booking, ticket, "Unable to reserve ticket");
                    continue;
                }

                // Create booking data in Redis (not DB)
                BookingData bookingData = new BookingData(
                        bookingReference,
                        booking.getUserId(),
                        ticketId,
                        ticket.getEventId(),
                        Enums.ConcurrencyType.HIGH,
                        booking.getAmount(),
                        expiredAt
                );

                redisService.storeBookingData(bookingData, ttlSeconds);

                // Create payment session
                PaymentRequest paymentRequest = new PaymentRequest(
                        bookingReference,
                        booking.getAmount(),
                        booking.getUserId()
                );

                PaymentResponse paymentResponse = paymentService.createPaymentSession(paymentRequest);

                // Update payment URL in Redis
                bookingData.setPaymentUrl(paymentResponse.getPaymentUrl());
                redisService.storeBookingData(bookingData, ttlSeconds);

                // Update queue status in DB (only status change)
                booking.setStatus(Enums.BookingStatus.PAYMENT_PENDING);
                bookingRepository.save(booking);

                successfulBookings.add(bookingReference);
                processedCount++;

                // Remove from processing
                redisService.removeFromProcessing(bookingReference);

                // Notify via SSE if client is connected
                queueStatusService.notifyBookingUpdate(bookingReference);

                // Send payment link notification
                NotificationRequest notificationRequest = new NotificationRequest(
                        booking.getUserId(),
                        bookingReference,
                        ticketId,
                        ticket.getEventName(),
                        booking.getAmount()
                );
                notificationRequest.setPaymentUrl(paymentResponse.getPaymentUrl());
                notificationRequest.setExpiredAt(expiredAt);

                notificationService.sendPaymentLink(notificationRequest);

                logger.info("Successfully processed booking from queue: {}, Payment URL: {}",
                        bookingReference, paymentResponse.getPaymentUrl());

            } catch (Exception e) {
                logger.error("Error processing booking: {}", bookingReference, e);
                redisService.removeFromProcessing(bookingReference);
                failedBookings.add(bookingReference);
            }
        }

        logger.info("Batch processing complete for ticket: {}. Successful: {}, Failed: {}, Total: {}",
                ticketId, successfulBookings.size(), failedBookings.size(), batch.size());

        // If ticket became unavailable, cancel remaining queue
        if (ticketBecameUnavailable) {
            cancelRemainingQueue(ticketId);
        }
    }

    /**
     * Cancel all remaining bookings in queue when ticket becomes unavailable
     */
    private void cancelRemainingQueue(String ticketId) {
        logger.info("Cancelling remaining queue for ticket: {}", ticketId);

        Long remainingSize = redisService.getQueueSize(ticketId);
        if (remainingSize == null || remainingSize == 0) {
            return;
        }

        // Pop all remaining items
        Set<Object> remaining = redisService.popBatchFromQueue(ticketId, remainingSize.intValue());

        Ticket ticket = ticketAvailabilityService.getTicket(ticketId);

        for (Object bookingRefObj : remaining) {
            String bookingReference = bookingRefObj.toString();

            try {
                Booking booking = bookingRepository.findByBookingReference(bookingReference)
                        .orElse(null);

                if (booking != null && booking.getStatus() == Enums.BookingStatus.QUEUED) {
                    booking.setStatus(Enums.BookingStatus.CANCELLED);
                    bookingRepository.save(booking);

                    // Notify user
                    sendFailureNotification(booking, ticket, "Ticket sold out");
                }

            } catch (Exception e) {
                logger.error("Error cancelling queued booking: {}", bookingReference, e);
            }
        }

        logger.info("Cancelled {} remaining bookings for ticket: {}", remaining.size(), ticketId);
    }

    /**
     * Send failure notification to user
     */
    private void sendFailureNotification(Booking booking, Ticket ticket, String reason) {
        try {
            NotificationRequest notificationRequest = new NotificationRequest(
                    booking.getUserId(),
                    booking.getBookingReference(),
                    booking.getTicketId(),
                    ticket != null ? ticket.getEventName() : "Unknown Event",
                    booking.getAmount()
            );
            notificationRequest.setFailureReason(reason);

            notificationService.sendBookingFailure(notificationRequest);
        } catch (Exception e) {
            logger.error("Failed to send notification for booking: {}", booking.getBookingReference(), e);
        }
    }

    /**
     * Clean up rate limiter windows
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000)
    public void cleanupRateLimiter() {
        logger.debug("Cleaning up rate limiter expired windows");
        rateLimiterService.cleanupExpiredWindows();
    }

    /**
     * Monitor queue health
     * Runs every 30 seconds
     */
    @Scheduled(fixedRate = 30000)
    public void monitorQueueHealth() {
        try {
            Set<String> activeQueues = redisService.getActiveQueueKeys();

            if (!activeQueues.isEmpty()) {
                logger.info("=== Queue Health Report ===");

                long totalQueued = 0;
                for (String queueKey : activeQueues) {
                    String ticketId = queueKey.replace("queue:", "");
                    Long size = redisService.getQueueSize(ticketId);

                    if (size != null && size > 0) {
                        totalQueued += size;
                        logger.info("Queue {}: {} bookings waiting", ticketId, size);
                    }
                }

                logger.info("Total queued bookings across all tickets: {}", totalQueued);
                logger.info("Active SSE connections: {}", queueStatusService.getActiveConnectionCount());
                logger.info("===========================");
            }

        } catch (Exception e) {
            logger.error("Error monitoring queue health", e);
        }
    }
}