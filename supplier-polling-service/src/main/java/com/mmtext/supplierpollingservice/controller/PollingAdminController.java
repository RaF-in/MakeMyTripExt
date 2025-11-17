package com.mmtext.supplierpollingservice.controller;

import com.mmtext.supplierpollingservice.domain.PollResult;
import com.mmtext.supplierpollingservice.domain.SupplierState;
import com.mmtext.supplierpollingservice.repo.PollResultRepository;
import com.mmtext.supplierpollingservice.repo.SupplierStateRepository;
import com.mmtext.supplierpollingservice.service.SupplierCircuitBreakerService;
import com.mmtext.supplierpollingservice.service.SupplierPollingScheduler;
import com.mmtext.supplierpollingservice.service.VolatilityAnalyzer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

@RestController
@RequestMapping("/api/polling")
public class PollingAdminController {

    private final SupplierStateRepository stateRepository;
    private final PollResultRepository resultRepository;
    private final SupplierCircuitBreakerService circuitBreakerService;
    private final SupplierPollingScheduler scheduler;
    private final VolatilityAnalyzer volatilityAnalyzer;

    public PollingAdminController(SupplierStateRepository stateRepository, PollResultRepository resultRepository, SupplierCircuitBreakerService circuitBreakerService, SupplierPollingScheduler scheduler, VolatilityAnalyzer volatilityAnalyzer) {
        this.stateRepository = stateRepository;
        this.resultRepository = resultRepository;
        this.circuitBreakerService = circuitBreakerService;
        this.scheduler = scheduler;
        this.volatilityAnalyzer = volatilityAnalyzer;
    }

    /**
     * Get all supplier states
     */
    @GetMapping("/suppliers/states")
    public Flux<SupplierState> getAllSupplierStates() {
        return stateRepository.findAll();
    }

    /**
     * Get specific supplier state
     */
    @GetMapping("/suppliers/{supplierId}/state")
    public Mono<SupplierState> getSupplierState(@PathVariable String supplierId) {
        return stateRepository.findById(supplierId);
    }

    /**
     * Get recent poll results for supplier
     */
    @GetMapping("/suppliers/{supplierId}/results")
    public Flux<PollResult> getRecentResults(
            @PathVariable String supplierId,
            @RequestParam(defaultValue = "24") int hours) {

        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        return resultRepository.findBySupplierIdAfter(supplierId, since);
    }

    /**
     * Get circuit breaker states for all suppliers
     */
    @GetMapping("/circuit-breakers")
    public Mono<Map<String, CircuitBreaker.State>> getCircuitBreakerStates() {
        return Mono.just(circuitBreakerService.getAllCircuitStates());
    }

    /**
     * Reset circuit breaker for specific supplier
     */
    @PostMapping("/suppliers/{supplierId}/circuit-breaker/reset")
    public Mono<String> resetCircuitBreaker(@PathVariable String supplierId) {
        circuitBreakerService.resetCircuitBreaker(supplierId);
        return Mono.just("Circuit breaker reset for: " + supplierId);
    }

    /**
     * Adjust polling interval
     */
    @PostMapping("/suppliers/{supplierId}/interval")
    public Mono<String> adjustInterval(
            @PathVariable String supplierId,
            @RequestParam long intervalMs) {

        scheduler.adjustPollingInterval(supplierId, intervalMs);
        return Mono.just("Polling interval adjusted for: " + supplierId);
    }

    /**
     * Stop polling for supplier
     */
    @PostMapping("/suppliers/{supplierId}/stop")
    public Mono<String> stopPolling(@PathVariable String supplierId) {
        scheduler.stopPolling(supplierId);
        return Mono.just("Polling stopped for: " + supplierId);
    }

    /**
     * Stream live poll results (Server-Sent Events)
     * This demonstrates reactive streaming to clients
     */
    @GetMapping(value = "/suppliers/{supplierId}/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PollResult> streamPollResults(@PathVariable String supplierId) {
        return Flux.interval(java.time.Duration.ofSeconds(5))
                .flatMap(tick -> resultRepository.findBySupplierId(supplierId).take(1));
    }
}
