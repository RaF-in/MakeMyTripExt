package com.mmtext.supplierpollingservice.service;


import com.mmtext.supplierpollingservice.poller.SupplierPoller;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class SupplierPollingScheduler {

    private static final Logger log = LoggerFactory.getLogger(SupplierPollingScheduler.class);
    private final SupplierPollingOrchestrator orchestrator;
    private final List<SupplierPoller> allPollers;

    private final ConcurrentHashMap<String, ScheduledExecutorService> schedulers =
            new ConcurrentHashMap<>();

    public SupplierPollingScheduler(SupplierPollingOrchestrator orchestrator, List<SupplierPoller> allPollers) {
        this.orchestrator = orchestrator;
        this.allPollers = allPollers;
    }

    @PostConstruct
    public void initializeSchedulers() {
        log.info("Initializing polling schedulers for {} suppliers", allPollers.size());

        // Each supplier gets its own scheduler with custom interval
        for (SupplierPoller poller : allPollers) {
            scheduleSupplierPolling(poller);
        }
    }

    /**
     * Schedule polling for individual supplier
     * Interval based on supplier type and demand patterns
     */
    private void scheduleSupplierPolling(SupplierPoller poller) {
        String supplierId = poller.supplierId();

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
//        executor.setThreadNamePrefix("poll-" + supplierId + "-");

        // Get polling interval from config (in production, from database)
        long intervalMs = determinePollingInterval(poller);

        executor.scheduleAtFixedRate(
                () -> {
                    try {
                        log.debug("Scheduled poll trigger for: {}", supplierId);

                        // Execute reactive poll (non-blocking)
                        orchestrator.executePoll(poller)
                                .doOnSuccess(result ->
                                        log.debug("Poll completed for: {}", supplierId))
                                .doOnError(ex ->
                                        log.error("Poll error for: {}", supplierId, ex))
                                .subscribe(); // Fire and forget

                    } catch (Exception e) {
                        log.error("Scheduled poll execution failed for: {}", supplierId, e);
                    }
                },
                0,               // Initial delay
                intervalMs,      // Period
                TimeUnit.MILLISECONDS
        );

        schedulers.put(supplierId, executor);
        log.info("Scheduled polling for supplier: {} every {}ms", supplierId, intervalMs);
    }

    /**
     * Determine polling interval based on supplier type and time of day
     * This mimics real-world adaptive polling
     */
    private long determinePollingInterval(SupplierPoller poller) {
        // In production: fetch from PollingConfig or dynamic based on volatility
        // For now: default intervals

        String supplierId = poller.supplierId();
        if (supplierId.contains("airline")) {
            return 120000; // 2 minutes for airlines
        } else if (supplierId.contains("hotel")) {
            return 300000; // 5 minutes for hotels
        } else {
            return 180000; // 3 minutes for buses
        }
    }

    /**
     * Alternative: Spring @Scheduled approach
     * Use this if you want centralized scheduling
     */
    @Scheduled(fixedRate = 120000, initialDelay = 5000)
    public void pollAirlineSuppliers() {
        allPollers.stream()
                .filter(p -> p.supplierId().contains("airline"))
                .forEach(poller -> {
                    orchestrator.executePoll(poller)
                            .subscribe(result ->
                                    log.debug("Airline poll completed: {}", result.getSupplierId()));
                });
    }

    @Scheduled(fixedRate = 300000, initialDelay = 10000)
    public void pollHotelSuppliers() {
        allPollers.stream()
                .filter(p -> p.supplierId().contains("hotel"))
                .forEach(poller -> {
                    orchestrator.executePoll(poller)
                            .subscribe(result ->
                                    log.debug("Hotel poll completed: {}", result.getSupplierId()));
                });
    }

    @Scheduled(fixedRate = 180000, initialDelay = 7000)
    public void pollBusSuppliers() {
        allPollers.stream()
                .filter(p -> p.supplierId().contains("bus"))
                .forEach(poller -> {
                    orchestrator.executePoll(poller)
                            .subscribe(result ->
                                    log.debug("Bus poll completed: {}", result.getSupplierId()));
                });
    }

    /**
     * Adjust polling interval dynamically (e.g., during peak hours)
     */
    public void adjustPollingInterval(String supplierId, long newIntervalMs) {
        ScheduledExecutorService oldScheduler = schedulers.get(supplierId);
        if (oldScheduler != null) {
            oldScheduler.shutdown();
        }

        SupplierPoller poller = allPollers.stream()
                .filter(p -> p.supplierId().equals(supplierId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + supplierId));

        scheduleSupplierPolling(poller);
        log.info("Adjusted polling interval for {} to {}ms", supplierId, newIntervalMs);
    }

    public void stopPolling(String supplierId) {
        ScheduledExecutorService scheduler = schedulers.remove(supplierId);
        if (scheduler != null) {
            scheduler.shutdown();
            log.info("Stopped polling for supplier: {}", supplierId);
        }
    }
}

