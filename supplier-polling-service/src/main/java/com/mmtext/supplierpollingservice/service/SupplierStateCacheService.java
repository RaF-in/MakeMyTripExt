package com.mmtext.supplierpollingservice.service;

import com.mmtext.supplierpollingservice.domain.SupplierState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SupplierStateCacheService {

    private static final Logger log = LoggerFactory.getLogger(SupplierStateCacheService.class);
    private static final String KEY_PREFIX = "state:";

    // Inject the specific template configured for SupplierState
    private final ReactiveRedisTemplate<String, SupplierState> redisTemplate;

    public SupplierStateCacheService(
            @Qualifier("reactiveSupplierStateRedisTemplate") ReactiveRedisTemplate<String, SupplierState> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Finds a SupplierState by its ID (the primary key in Redis).
     */
    public Mono<SupplierState> getStateById(String supplierId) {
        String key = KEY_PREFIX + supplierId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Saves the SupplierState to Redis.
     */
    public Mono<SupplierState> saveState(SupplierState state) {
        String key = KEY_PREFIX + state.getSupplierId();

        // Use set and return the saved object
        return redisTemplate.opsForValue()
                .set(key, state)
                .thenReturn(state)
                .doOnError(ex -> log.error("Error saving SupplierState to Redis for ID: {}", state.getSupplierId(), ex))
                .onErrorResume(ex -> Mono.empty()); // Return empty Mono on failure to avoid blocking
    }
}