package com.mmtext.supplierpollingservice.service;

import com.mmtext.supplierpollingservice.domain.PollResult;
import com.mmtext.supplierpollingservice.domain.SupplierState;
import com.mmtext.supplierpollingservice.enums.PollStatus;
import com.mmtext.supplierpollingservice.enums.SupplierHealth;
import com.mmtext.supplierpollingservice.poller.SupplierPoller;
import com.mmtext.supplierpollingservice.repo.PollResultRepository;
import com.mmtext.supplierpollingservice.repo.SupplierStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class SupplierPollingOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(SupplierPollingOrchestrator.class);
    private final SupplierStateRepository stateRepository;
    private final PollResultRepository resultRepository;
    private final SupplierCircuitBreakerService circuitBreakerService;

    public SupplierPollingOrchestrator(SupplierStateRepository stateRepository, PollResultRepository resultRepository, SupplierCircuitBreakerService circuitBreakerService) {
        this.stateRepository = stateRepository;
        this.resultRepository = resultRepository;
        this.circuitBreakerService = circuitBreakerService;
    }

    /**
     * Executes single poll operation for a supplier
     * This is called by scheduled workers, NOT continuous reactive stream
     */
    public Mono<PollResult> executePoll(SupplierPoller poller) {
        String supplierId = poller.supplierId();

        log.debug("Starting poll execution for supplier: {}", supplierId);

        return stateRepository.findById(supplierId)
                .defaultIfEmpty(createDefaultState(supplierId))
                .flatMap(state -> {

                    // Check if supplier should be backed off due to failures
                    if (state.shouldBackoff()) {
                        long backoffDelay = state.getBackoffDelayMs();
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
     * Handles poll result: updates state, persists result
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

            log.info("Poll successful for supplier: {} ({})",
                    state.getSupplierId(), result.getStatus());

        } else {
            state.markFailure();
            log.warn("Poll failed for supplier: {} - {}",
                    state.getSupplierId(), result.getErrorMessage());
        }

        // Persist state and result
        return stateRepository.save(state)
                .then(resultRepository.save(result))
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
