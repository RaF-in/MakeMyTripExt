package com.mmtext.supplierpollingservice.service;


import com.mmtext.supplierpollingservice.config.PollingConfig;
import com.mmtext.supplierpollingservice.enums.SupplierType;
import com.mmtext.supplierpollingservice.poller.AirlineSupplierPoller;
import com.mmtext.supplierpollingservice.poller.BusSupplierPoller;
import com.mmtext.supplierpollingservice.poller.HotelSupplierPoller;
import com.mmtext.supplierpollingservice.poller.SupplierPoller;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class SupplierPollingScheduler {

    private static final Logger log = LoggerFactory.getLogger(SupplierPollingScheduler.class);
    private final SupplierPollingOrchestrator orchestrator;
    private final List<SupplierPoller> allPollers = new ArrayList<>();

    private final WebClient.Builder webClientBuilder;

    private final ConcurrentHashMap<String, ScheduledExecutorService> schedulers =
            new ConcurrentHashMap<>();

    public SupplierPollingScheduler(
            SupplierPollingOrchestrator orchestrator,
            WebClient.Builder webClientBuilder
    ) {
        this.orchestrator = orchestrator;
        this.webClientBuilder = webClientBuilder;

        // Hard-code pollers directly here
        //allPollers.add(new AirlineSupplierPoller(this.webClientBuilder, airlineSupplierConfig()));
        allPollers.add(new HotelSupplierPoller(this.webClientBuilder, hotelSupplierConfig()));
        //allPollers.add(new BusSupplierPoller(this.webClientBuilder, busSupplierConfig()));
    }

    @PostConstruct
    public void initializeSchedulers() {
        log.info("Initializing polling schedulers for {} suppliers", allPollers.size());

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
            return 60000; // 5 minutes for hotels
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


    public PollingConfig airlineSupplierConfig() {
        return PollingConfig.airlineDefault(
                "airline-amadeus-1",
                "http://listing-service:4000"
        );
    }


    public PollingConfig hotelSupplierConfig() {
        return PollingConfig.hotelDefault(
                "hotel-booking-1",
                "http://listing-service:4000"
        );
    }


    public PollingConfig busSupplierConfig() {
        PollingConfig config = new PollingConfig();
        config.setSupplierId("bus-amadeus-1");
        config.setSupplierType(SupplierType.BUS);
        config.setBaseUrl("http://listing-service:4000");
        config.setNormalIntervalMs(180000L); // 3 min
        config.setPeakIntervalMs(60000L);    // 1 min
        config.setMaxRetries(3);
        config.setTimeoutMs(180000L);
        config.setSupportsEtag(false);
        config.setSupportsIfModifiedSince(true);
        return config;
    }
}

