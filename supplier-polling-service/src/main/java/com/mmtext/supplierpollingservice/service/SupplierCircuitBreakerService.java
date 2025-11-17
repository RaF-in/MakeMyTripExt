package com.mmtext.supplierpollingservice.service;

import com.mmtext.supplierpollingservice.domain.PollResult;
import com.mmtext.supplierpollingservice.enums.PollStatus;
import com.mmtext.supplierpollingservice.enums.SupplierType;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SupplierCircuitBreakerService {

    private static final Logger log = LoggerFactory.getLogger(SupplierCircuitBreakerService.class);
    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
    private final CircuitBreakerRegistry registry;

    public SupplierCircuitBreakerService() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f)              // Open if 50% fail
                .waitDurationInOpenState(Duration.ofSeconds(60))  // Stay open 60s
                .slidingWindowSize(10)                     // Track last 10 calls
                .minimumNumberOfCalls(5)                   // Need 5 calls before evaluating
                .permittedNumberOfCallsInHalfOpenState(3)  // Test with 3 calls
                .slowCallDurationThreshold(Duration.ofSeconds(15)) // Call slow if > 15s
                .slowCallRateThreshold(50.0f)              // Open if 50% slow
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        this.registry = CircuitBreakerRegistry.of(config);
    }

    /**
     * Wraps reactive polling call with circuit breaker
     * Returns fallback on circuit open or failure
     */
    public Mono<PollResult> executeWithCircuitBreaker(
            String supplierId,
            Mono<PollResult> pollOperation,
            SupplierType type) {

        CircuitBreaker cb = getOrCreateCircuitBreaker(supplierId);

        return pollOperation
                .transformDeferred(CircuitBreakerOperator.of(cb))
                .doOnNext(result -> log.debug("Poll succeeded for: {}", supplierId))
                .onErrorResume(ex -> {
                    log.warn("Circuit breaker caught error for supplier: {}", supplierId, ex);
                    return Mono.just(PollResult.failure(supplierId, type,
                            "Circuit breaker: " + ex.getMessage()));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Circuit open for supplier: {}", supplierId);
                    PollResult pollResult = new PollResult();
                    pollResult.setSupplierId(supplierId);
                    pollResult.setSupplierType(type);
                    pollResult.setStatus(PollStatus.CIRCUIT_OPEN);
                    return Mono.just(pollResult);
                }));
    }

    private CircuitBreaker getOrCreateCircuitBreaker(String supplierId) {
        return circuitBreakers.computeIfAbsent(supplierId,
                id -> {
                    CircuitBreaker cb = registry.circuitBreaker(id);

                    // Log state transitions
                    cb.getEventPublisher()
                            .onStateTransition(event ->
                                    log.warn("Supplier {} circuit breaker: {} -> {}",
                                            id, event.getStateTransition().getFromState(),
                                            event.getStateTransition().getToState())
                            );

                    return cb;
                });
    }

    public CircuitBreaker.State getCircuitState(String supplierId) {
        CircuitBreaker cb = circuitBreakers.get(supplierId);
        return cb != null ? cb.getState() : CircuitBreaker.State.CLOSED;
    }

    public Map<String, CircuitBreaker.State> getAllCircuitStates() {
        Map<String, CircuitBreaker.State> states = new ConcurrentHashMap<>();
        circuitBreakers.forEach((id, cb) -> states.put(id, cb.getState()));
        return states;
    }

    public void resetCircuitBreaker(String supplierId) {
        CircuitBreaker cb = circuitBreakers.get(supplierId);
        if (cb != null) {
            cb.reset();
            log.info("Reset circuit breaker for supplier: {}", supplierId);
        }
    }
}
