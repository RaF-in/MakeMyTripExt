package com.mmtext.supplierpollingservice.service;

import com.mmtext.supplierpollingservice.domain.PollResult;
import com.mmtext.supplierpollingservice.domain.SupplierState;
import com.mmtext.supplierpollingservice.enums.PollStatus;
import com.mmtext.supplierpollingservice.enums.SupplierHealth;
import com.mmtext.supplierpollingservice.poller.SupplierPoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class SupplierPollingOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SupplierPollingOrchestrator.class);

    // Replaced Repositories with dedicated Redis Cache Services
    private final SupplierStateCacheService stateCacheService;
    private final PollResultCacheService resultCacheService;
    private final SupplierCircuitBreakerService circuitBreakerService;

    public SupplierPollingOrchestrator(
            SupplierStateCacheService stateCacheService,
            PollResultCacheService resultCacheService,
            SupplierCircuitBreakerService circuitBreakerService) {
        this.stateCacheService = stateCacheService;
        this.resultCacheService = resultCacheService;
        this.circuitBreakerService = circuitBreakerService;
    }

    /**
     * Executes single poll operation for a supplier.
     * Uses Redis for fetching/updating SupplierState.
     */
    public Mono<PollResult> executePoll(SupplierPoller poller) {
        String supplierId = poller.supplierId();

        log.debug("Starting poll execution for supplier: {}", supplierId);

        // stateCacheService.getStateById() replaces stateRepository.findById()
        return stateCacheService.getStateById(supplierId)
                .defaultIfEmpty(createDefaultState(supplierId))
                .flatMap(state -> {

                    // Check if supplier should be backed off due to failures
                    int consecutiveFailures = state.getConsecutiveFailures();
                    if (consecutiveFailures >= 3) {
                        long backoffDelay = Math.min((long) Math.pow(2, consecutiveFailures) * 5000, 300000); // Max 5 min
                        long timeSinceLastPoll = System.currentTimeMillis() -
                                state.getLastPolledAt().toEpochMilli();

                        if (timeSinceLastPoll < backoffDelay) {
                            log.info("Supplier {} in backoff, skipping poll", supplierId);
                            return Mono.just(PollResult.failure(supplierId,
                                    state.getSupplierType(), "Backoff active"));
                        }
                    }

                    // Execute poll with circuit breaker protection
                    return circuitBreakerService.executeWithCircuitBreaker(
                                    supplierId,
                                    poller.poll(state),
                                    state.getSupplierType()
                            )
                            .flatMap(result -> handlePollResult(result, state));
                })
                .subscribeOn(Schedulers.boundedElastic()); // Non-blocking I/O scheduler
    }

    /**
     * Handles poll result: updates state, persists result using Redis services.
     */
    private Mono<PollResult> handlePollResult(PollResult result, SupplierState state) {

        // Update supplier state based on result
        if (result.getStatus() == PollStatus.SUCCESS ||
                result.getStatus() == PollStatus.NOT_MODIFIED) {

            state.markSuccess();

            // Update ETag and cursor for next poll
            if (result.getNewEtag() != null) {
                state.setEtag(result.getNewEtag());
            }
            if (result.getNewCursor() != null) {
                state.setCursor(result.getNewCursor());
            }

            state.setLastModifiedAt(result.getPolledAt());

            log.info("Poll successful for supplier: {} ({})",
                    state.getSupplierId(), result.getStatus());

        } else {
            state.markFailure();
            log.warn("Poll failed for supplier: {} - {}",
                    state.getSupplierId(), result.getErrorMessage());
        }

        // Persist state and result using Redis services (replace repository saves)
        // Note: The execution order is preserved via 'then'.
        return stateCacheService.saveState(state) // stateRepository.save(state)
                .then(resultCacheService.savePollResult(result)) // resultRepository.save(result)
                .thenReturn(result);
    }

    private SupplierState createDefaultState(String supplierId) {
        SupplierState supplierState = new SupplierState();
        supplierState.setSupplierId(supplierId);
        supplierState.setHealth(SupplierHealth.HEALTHY);
        supplierState.setConsecutiveFailures(0);
        return supplierState;
    }
}