package com.mmtext.bookingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mmtext.bookingservice.dto.BookingData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;
    private static final String ACTIVE_QUEUES_SET = "active_queues";
    public RedisService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    private static final String RESERVATION_PREFIX = "reservation:";
    private static final String BOOKING_DATA_PREFIX = "booking:";
    private static final String QUEUE_PREFIX = "queue:";
    private static final String PROCESSING_PREFIX = "processing:";
    private static final String TICKET_LOCK_PREFIX = "ticket:";

    /**
     * Store booking data in Redis with TTL (for PAYMENT_PENDING state)
     */
    public void storeBookingData(BookingData bookingData, long ttlSeconds) {
        String key = BOOKING_DATA_PREFIX + bookingData.getBookingReference();
        try {
            String json = objectMapper.writeValueAsString(bookingData);
            redisTemplate.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize booking data", e);
        }
    }

    /**
     * Get booking data from Redis
     */
    public BookingData getBookingData(String bookingReference) {
        String key = BOOKING_DATA_PREFIX + bookingReference;
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        try {
            return objectMapper.readValue(value.toString(), BookingData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize booking data", e);
        }
    }

    /**
     * Delete booking data from Redis
     */
    public void deleteBookingData(String bookingReference) {
        String key = BOOKING_DATA_PREFIX + bookingReference;
        redisTemplate.delete(key);
    }

    /**
     * Lock ticket atomically (used for all concurrency types)
     */
    public boolean lockTicket(String ticketId, String bookingReference, long ttlSeconds) {
        String key = TICKET_LOCK_PREFIX + ticketId;

        // Lua script to check if ticket is available and lock atomically
        String luaScript =
                "if redis.call('EXISTS', KEYS[1]) == 0 then " +
                        "    redis.call('SETEX', KEYS[1], ARGV[1], ARGV[2]) " +
                        "    return 1 " +
                        "else " +
                        "    return 0 " +
                        "end";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                ttlSeconds,
                bookingReference
        );

        return result != null && result == 1;
    }

    /**
     * Unlock ticket
     */
    public void unlockTicket(String ticketId) {
        String key = TICKET_LOCK_PREFIX + ticketId;
        redisTemplate.delete(key);
    }

    /**
     * Check if ticket is locked
     */
    public boolean isTicketLocked(String ticketId) {
        String key = TICKET_LOCK_PREFIX + ticketId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Get all locked ticket IDs for an event (used when checking availability)
     */
    public Set<String> getLockedTicketIds(String eventId) {
        String pattern = TICKET_LOCK_PREFIX + eventId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys == null || keys.isEmpty()) {
            return Set.of();
        }

        // Extract ticket IDs from keys
        return keys.stream()
                .map(key -> key.replace(TICKET_LOCK_PREFIX, ""))
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Get queue position
     */
    public Long getQueuePosition(String ticketId, String bookingReference) {
        String key = QUEUE_PREFIX + ticketId;
        return redisTemplate.opsForZSet().rank(key, bookingReference);
    }

    /**
     * Get queue size
     */
    public Long getQueueSize(String ticketId) {
        String key = QUEUE_PREFIX + ticketId;
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * Mark booking as processing
     */
    public void markAsProcessing(String bookingReference, long ttlSeconds) {
        String key = PROCESSING_PREFIX + bookingReference;
        redisTemplate.opsForValue().set(key, "1", ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * Check if booking is processing
     */
    public boolean isProcessing(String bookingReference) {
        String key = PROCESSING_PREFIX + bookingReference;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Remove from processing
     */
    public void removeFromProcessing(String bookingReference) {
        String key = PROCESSING_PREFIX + bookingReference;
        redisTemplate.delete(key);
    }


    /**
     * Add ticket to active queues set
     * Called when first booking is added to queue
     */
    public void registerActiveQueue(String ticketId) {
        redisTemplate.opsForSet().add(ACTIVE_QUEUES_SET, ticketId);
        log.debug("Registered active queue for ticket: {}", ticketId);
    }

    /**
     * Remove ticket from active queues set
     * Called when queue becomes empty
     */
    public void deregisterActiveQueue(String ticketId) {
        redisTemplate.opsForSet().remove(ACTIVE_QUEUES_SET, ticketId);
        log.debug("Deregistered active queue for ticket: {}", ticketId);
    }

    /**
     * Get all active queue ticket IDs
     */
    public Set<String> getActiveQueueKeys() {
        Set<Object> rawKeys = redisTemplate.opsForSet().members(ACTIVE_QUEUES_SET);

        if (rawKeys == null || rawKeys.isEmpty()) {
            return Set.of();
        }

        return rawKeys.stream()
                .map(Object::toString)
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Check if queue is active
     */
    public boolean isQueueActive(String ticketId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ACTIVE_QUEUES_SET, ticketId));
    }

    /**
     * Update the addToQueue method to register active queue
     */
    public void addToQueue(String ticketId, String bookingReference) {
        String key = QUEUE_PREFIX + ticketId;
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(key, bookingReference, score);

        // Register as active queue
        registerActiveQueue(ticketId);

        log.debug("Added {} to queue {}", bookingReference, ticketId);
    }

    /**
     * Update the popBatchFromQueue method to deregister when empty
     */
    public Set<Object> popBatchFromQueue(String ticketId, int batchSize) {
        String key = QUEUE_PREFIX + ticketId;

        // Lua script to atomically get and remove items
        String luaScript =
                "local items = redis.call('ZRANGE', KEYS[1], 0, ARGV[1] - 1) " +
                        "if #items > 0 then " +
                        "    redis.call('ZREMRANGEBYRANK', KEYS[1], 0, ARGV[1] - 1) " +
                        "end " +
                        "local remaining = redis.call('ZCARD', KEYS[1]) " +
                        "return {items, remaining}";

        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(List.class);

        List<Object> result = redisTemplate.execute(
                script,
                Collections.singletonList(key),
                batchSize
        );

        if (result == null || result.isEmpty()) {
            return Set.of();
        }

        // Extract items and remaining count
        List<Object> items = (List<Object>) result.get(0);
        Long remaining = result.size() > 1 ? Long.valueOf(result.get(1).toString()) : 0L;

        // Deregister if queue is now empty
        if (remaining == 0) {
            deregisterActiveQueue(ticketId);
            log.info("Queue empty for ticket: {}, deregistered", ticketId);
        }

        return items != null ? Set.copyOf(items) : Set.of();
    }
}
