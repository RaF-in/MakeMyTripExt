package com.mmtext.supplierpollingservice.service;

import com.mmtext.supplierpollingservice.domain.PollResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class PollResultCacheService {

    private static final Logger log = LoggerFactory.getLogger(PollResultCacheService.class);
    // 7 Days TTL
    private static final Duration POLL_RESULT_TTL = Duration.ofDays(7);

    // Key prefix changed to prioritize supplier ID
    private static final String KEY_PREFIX = "supplier:";

    // Inject the specific template configured for PollResult
    private final ReactiveRedisTemplate<String, PollResult> redisTemplate;

    public PollResultCacheService(
            @Qualifier("reactivePollResultRedisTemplate") ReactiveRedisTemplate<String, PollResult> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Replaces the PollResultRepository. Saves the PollResult using a composite key:
     * supplier:{supplierId}:poll:{pollResultId} with a 7-day TTL.
     */
    public Mono<PollResult> savePollResult(PollResult pollResult) {
        // Construct the composite key
        String key = KEY_PREFIX + pollResult.getSupplierId() + ":poll:" + pollResult.getId();

        log.debug("Saving PollResult with composite key: {} and TTL: {}", key, POLL_RESULT_TTL);

        // Uses set(key, value, duration) and returns the saved object
        return redisTemplate.opsForValue()
                .set(key, pollResult, POLL_RESULT_TTL)
                .thenReturn(pollResult)
                .doOnError(ex -> log.error("Error saving PollResult to Redis", ex))
                .onErrorResume(ex -> Mono.empty()); // Return empty Mono on failure to avoid blocking
    }

    // findPollResultsBySupplierId method from previous answer would also go here.
}