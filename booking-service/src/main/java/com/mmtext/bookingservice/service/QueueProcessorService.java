package com.mmtext.bookingservice.service;

import com.mmtext.bookingservice.dto.BookingData;
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
import org.springframework.transaction.annotation.Transactional;

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

    @Value("${booking.queue.batch-size:100}")
    private int batchSize;

    /**
     * Process queue in batches
     * Runs every 5 seconds
     */
    @Scheduled(fixedDelayString = "${booking.queue.processing-interval:5000}")
    @Transactional
    public void processQueue() {
        logger.debug("Processing high concurrency queues...");

        // In a real system, you'd track which ticket IDs have active queues
        // For this example, we'll process queues for known ticket patterns
        // In production, you'd maintain a separate set of active queue IDs

        List<String> activeTicketIds = getActiveQueueTicketIds();

        for (String ticketId : activeTicketIds) {
            processQueueForTicket(ticketId);
        }
    }

    /**
     * Process queue for a specific ticket - Redis-first approach
     */
    private void processQueueForTicket(String ticketId) {
        Long queueSize = redisService.getQueueSize(ticketId);

        if (queueSize == null || queueSize == 0) {
            return;
        }

        logger.info("Processing queue for ticket: {}, Queue size: {}", ticketId, queueSize);

        // Pop batch from queue
        Set<Object> batch = redisService.popBatchFromQueue(ticketId, batchSize);

        if (batch.isEmpty()) {
            return;
        }

        logger.info("Processing batch of {} bookings for ticket: {}", batch.size(), ticketId);

        List<String> successfulBookings = new ArrayList<>();
        Instant expiredAt = Instant.now().plus(15, ChronoUnit.MINUTES);
        long ttlSeconds = Duration.between(Instant.now(), expiredAt).getSeconds();

        // Get ticket info
        Ticket ticket = ticketAvailabilityService.getTicket(ticketId);
        if (ticket == null) {
            logger.error("Ticket not found: {}", ticketId);
            return;
        }

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
                    continue;
                }

                if (booking.getStatus() != Enums.BookingStatus.QUEUED) {
                    logger.warn("Booking not in QUEUED status: {}", bookingReference);
                    redisService.removeFromProcessing(bookingReference);
                    continue;
                }

                // Lock ticket in Redis atomically
                boolean locked = redisService.lockTicket(ticketId, bookingReference, ttlSeconds);

                if (!locked) {
                    logger.warn("Failed to lock ticket for booking: {}", bookingReference);
                    booking.setStatus(Enums.BookingStatus.CANCELLED);
                    bookingRepository.save(booking);
                    redisService.removeFromProcessing(bookingReference);
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

                // Remove from processing
                redisService.removeFromProcessing(bookingReference);

                // Notify via SSE if client is connected
                queueStatusService.notifyBookingUpdate(bookingReference);

                logger.info("Successfully processed booking from queue: {}, Payment URL: {}",
                        bookingReference, paymentResponse.getPaymentUrl());

            } catch (Exception e) {
                logger.error("Error processing booking: {}", bookingReference, e);
                redisService.removeFromProcessing(bookingReference);
            }
        }

        logger.info("Batch processing complete for ticket: {}. Successful: {}/{}",
                ticketId, successfulBookings.size(), batch.size());
    }

    /**
     * Get list of active queue ticket IDs
     * In production, this would be maintained in Redis or DB
     */
    private List<String> getActiveQueueTicketIds() {
        // This is a placeholder. In production, you would:
        // 1. Maintain a Redis Set of active queue ticket IDs
        // 2. Add ticket ID to set when first booking is queued
        // 3. Remove from set when queue is empty
        // 4. Query this set here

        // For demo purposes, scan for any HIGH concurrency bookings in QUEUED status
        // In production, use a better approach

        return new ArrayList<>();
    }
}