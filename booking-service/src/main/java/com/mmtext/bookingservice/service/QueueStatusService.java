package com.mmtext.bookingservice.service;

import com.mmtext.bookingservice.dto.PaymentRequest;
import com.mmtext.bookingservice.dto.PaymentResponse;
import com.mmtext.bookingservice.dto.QueueStatusUpdate;
import com.mmtext.bookingservice.enums.Enums;
import com.mmtext.bookingservice.model.Booking;
import com.mmtext.bookingservice.repo.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class QueueStatusService {

    private static final Logger logger = LoggerFactory.getLogger(QueueStatusService.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private PaymentService paymentService;

    // Store active SSE connections
    private final Map<String, SseEmitter> activeConnections = new ConcurrentHashMap<>();

    // Single-threaded scheduler for all SSE updates to prevent thundering herd
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // Random instance for jitter
    private final Random random = new Random();

    // Configuration
    private static final long SSE_TIMEOUT = 300_000; // 5 minutes
    private static final long BASE_UPDATE_INTERVAL = 3000; // 3 seconds base interval
    private static final long MAX_JITTER = 2000; // Up to 2 seconds jitter

    /**
     * Create SSE connection for queue status updates
     * Uses exponential backoff hint and jitter to prevent thundering herd
     */
    public SseEmitter createQueueStatusStream(String bookingReference) {
        logger.info("Creating SSE stream for booking: {}", bookingReference);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // Add to active connections
        activeConnections.put(bookingReference, emitter);

        // Set up cleanup on completion/timeout/error
        Runnable cleanup = () -> {
            activeConnections.remove(bookingReference);
            logger.info("SSE stream closed for booking: {}", bookingReference);
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> {
            logger.error("SSE error for booking: {}", bookingReference, e);
            cleanup.run();
        });

        // Send initial connection event with retry configuration
        try {
            // Client should retry with exponential backoff: 3s, 6s, 12s, max 30s
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{\"message\":\"Connected to queue status stream\"}")
                    .reconnectTime(3000)); // Initial retry after 3 seconds

            // Schedule first update with initial jitter (0-2 seconds)
            long initialDelay = random.nextInt((int) MAX_JITTER);
            scheduleUpdate(bookingReference, emitter, initialDelay);

        } catch (IOException e) {
            logger.error("Failed to send initial SSE event", e);
            activeConnections.remove(bookingReference);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * Schedule update with jitter to prevent thundering herd
     */
    private void scheduleUpdate(String bookingReference, SseEmitter emitter, long delay) {
        scheduler.schedule(() -> {
            try {
                if (!activeConnections.containsKey(bookingReference)) {
                    return; // Connection already closed
                }

                QueueStatusUpdate status = getQueueStatus(bookingReference);

                if (status.getStatus().equals("COMPLETED") ||
                        status.getStatus().equals("CANCELLED") ||
                        status.getStatus().equals("EXPIRED")) {

                    // Send final status and close
                    emitter.send(SseEmitter.event()
                            .name("queue-status")
                            .data(status));

                    emitter.send(SseEmitter.event()
                            .name("done")
                            .data("{\"message\":\"Queue processing complete\"}"));

                    emitter.complete();
                    activeConnections.remove(bookingReference);

                } else if (status.getStatus().equals("PAYMENT_PENDING")) {

                    // Send payment URL and close
                    emitter.send(SseEmitter.event()
                            .name("queue-status")
                            .data(status));

                    emitter.send(SseEmitter.event()
                            .name("redirect")
                            .data(status));

                    emitter.complete();
                    activeConnections.remove(bookingReference);

                } else {
                    // Still in queue, send update
                    emitter.send(SseEmitter.event()
                            .name("queue-status")
                            .data(status)
                            .reconnectTime(calculateReconnectTime(status)));

                    // Schedule next update with jitter
                    long nextDelay = BASE_UPDATE_INTERVAL + random.nextInt((int) MAX_JITTER);
                    scheduleUpdate(bookingReference, emitter, nextDelay);
                }

            } catch (IOException e) {
                logger.error("Failed to send SSE update for: {}", bookingReference, e);
                activeConnections.remove(bookingReference);
                emitter.completeWithError(e);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Get current queue status
     */
    private QueueStatusUpdate getQueueStatus(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        QueueStatusUpdate update = new QueueStatusUpdate();
        update.setBookingReference(bookingReference);
        update.setStatus(booking.getStatus().name());

        if (booking.getStatus() == Enums.BookingStatus.QUEUED) {
            Long position = redisService.getQueuePosition(booking.getTicketId(), bookingReference);
            Long queueSize = redisService.getQueueSize(booking.getTicketId());

            update.setQueuePosition(position != null ? position.intValue() + 1 : null);
            update.setTotalInQueue(queueSize != null ? queueSize.intValue() : null);
            update.setEstimatedWaitSeconds(calculateEstimatedWait(position));

        } else if (booking.getStatus() == Enums.BookingStatus.PAYMENT_PENDING) {
            // Create payment request if not exists
            if (booking.getPaymentId() == null) {
                PaymentRequest paymentRequest = new PaymentRequest(
                        bookingReference,
                        booking.getAmount(),
                        booking.getUserId()
                );

                PaymentResponse paymentResponse = paymentService.createPaymentSession(paymentRequest);
                update.setPaymentUrl(paymentResponse.getPaymentUrl());
            }
            //update.setExpiredAt(booking.getExpiredAt());
        }

        return update;
    }

    /**
     * Calculate estimated wait time based on position
     * Assumes batch processing every 5 seconds for 100 people
     */
    private Integer calculateEstimatedWait(Long position) {
        if (position == null) return null;

        int batchSize = 100;
        int processingInterval = 5; // seconds

        int batchesAhead = (int) Math.ceil(position.doubleValue() / batchSize);
        return batchesAhead * processingInterval;
    }

    /**
     * Calculate reconnect time with exponential backoff based on queue position
     * Those further back in queue get longer reconnect times
     */
    private long calculateReconnectTime(QueueStatusUpdate status) {
        if (status.getQueuePosition() == null) {
            return 5000; // Default 5 seconds
        }

        // Exponential backoff based on position
        // Position 1-100: 3s, 101-500: 5s, 501-1000: 8s, 1000+: 12s
        if (status.getQueuePosition() <= 100) {
            return 3000;
        } else if (status.getQueuePosition() <= 500) {
            return 5000;
        } else if (status.getQueuePosition() <= 1000) {
            return 8000;
        } else {
            return 12000;
        }
    }

    /**
     * Broadcast update to specific booking (called from queue processor)
     */
    public void notifyBookingUpdate(String bookingReference) {
        SseEmitter emitter = activeConnections.get(bookingReference);

        if (emitter != null) {
            try {
                QueueStatusUpdate status = getQueueStatus(bookingReference);
                emitter.send(SseEmitter.event()
                        .name("queue-status")
                        .data(status));

                // If moved to payment pending, close connection
                if (status.getStatus().equals("PAYMENT_PENDING")) {
                    emitter.send(SseEmitter.event()
                            .name("redirect")
                            .data(status));
                    emitter.complete();
                    activeConnections.remove(bookingReference);
                }

            } catch (IOException e) {
                logger.error("Failed to send notification for: {}", bookingReference, e);
                activeConnections.remove(bookingReference);
            }
        }
    }

    /**
     * Get count of active SSE connections
     */
    public int getActiveConnectionCount() {
        return activeConnections.size();
    }

    /**
     * Shutdown scheduler gracefully
     */
    public void shutdown() {
        logger.info("Shutting down queue status service");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Close all active connections
        activeConnections.values().forEach(SseEmitter::complete);
        activeConnections.clear();
    }
}