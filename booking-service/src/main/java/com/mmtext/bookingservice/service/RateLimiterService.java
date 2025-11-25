package com.mmtext.bookingservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter to prevent connection abuse
 * Uses sliding window algorithm
 */
@Service
public class RateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);

    // Track connection attempts per booking reference
    private final Map<String, ConnectionWindow> connectionAttempts = new ConcurrentHashMap<>();

    // Configuration
    private static final int MAX_CONNECTIONS_PER_MINUTE = 10;
    private static final Duration WINDOW_DURATION = Duration.ofMinutes(1);

    /**
     * Check if connection is allowed
     */
    public boolean allowConnection(String bookingReference) {
        ConnectionWindow window = connectionAttempts.compute(bookingReference, (key, existing) -> {
            Instant now = Instant.now();

            if (existing == null || existing.isExpired(now)) {
                return new ConnectionWindow(now);
            }

            existing.incrementCount();
            return existing;
        });

        boolean allowed = window.getCount() <= MAX_CONNECTIONS_PER_MINUTE;

        if (!allowed) {
            logger.warn("Rate limit exceeded for booking: {}. Count: {}",
                    bookingReference, window.getCount());
        }

        return allowed;
    }

    /**
     * Clean up expired windows periodically
     */
    public void cleanupExpiredWindows() {
        Instant now = Instant.now();
        connectionAttempts.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    /**
     * Connection window for rate limiting
     */
    private static class ConnectionWindow {
        private final Instant startTime;
        private int count;

        public ConnectionWindow(Instant startTime) {
            this.startTime = startTime;
            this.count = 1;
        }

        public void incrementCount() {
            this.count++;
        }

        public int getCount() {
            return count;
        }

        public boolean isExpired(Instant now) {
            return Duration.between(startTime, now).compareTo(WINDOW_DURATION) > 0;
        }
    }
}