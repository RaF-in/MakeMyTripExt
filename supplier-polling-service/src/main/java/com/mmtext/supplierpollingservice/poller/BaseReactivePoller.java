package com.mmtext.supplierpollingservice.poller;

import com.mmtext.supplierpollingservice.config.PollingConfig;
import com.mmtext.supplierpollingservice.domain.PollResult;
import com.mmtext.supplierpollingservice.domain.SupplierState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public abstract class BaseReactivePoller implements SupplierPoller {

    private static final Logger log = LoggerFactory.getLogger(BaseReactivePoller.class);
    protected final WebClient webClient;
    protected final PollingConfig config;

    private static final DateTimeFormatter RFC_1123 =
            DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);

    protected BaseReactivePoller(WebClient.Builder webClientBuilder, PollingConfig config) {
        this.config = config;
        this.webClient = webClientBuilder
                .baseUrl(config.getBaseUrl())
                .defaultHeader(HttpHeaders.USER_AGENT, "BookingPoller/1.0")
                .defaultHeader("X-API-Key", config.getApiKey())
                .build();
    }

    @Override
    public String supplierId() {
        return config.getSupplierId();
    }

    /**
     * Prepares GET request with conditional headers (ETag, If-Modified-Since)
     * This is the KEY to reducing bandwidth - suppliers return 304 if unchanged
     */
    protected WebClient.RequestHeadersSpec<?> prepareConditionalGet(
            String path, SupplierState state) {

        WebClient.RequestHeadersSpec<?> spec = webClient.get().uri(path);

        // Prefer ETag (stronger validator)
        if (config.isSupportsEtag() && state.getEtag() != null) {
            spec = spec.header(HttpHeaders.IF_NONE_MATCH, state.getEtag());
            log.debug("Using ETag: {} for supplier: {}", state.getEtag(), supplierId());
        }
        // Fallback to If-Modified-Since
        else if (config.isSupportsIfModifiedSince() && state.getLastModifiedAt() != null) {
            String headerValue = RFC_1123.format(state.getLastModifiedAt());
            spec = spec.header(HttpHeaders.IF_MODIFIED_SINCE, headerValue);
            log.debug("Using If-Modified-Since: {} for supplier: {}", headerValue, supplierId());
        }

        return spec;
    }

    /**
     * Handles HTTP response with proper status code checking
     */
    protected Mono<PollResult> handleResponse(
            Mono<ClientResponse> responseMono,
            SupplierState state
    ) {

        return responseMono
                .timeout(Duration.ofMillis(config.getTimeoutMs()))
                .flatMap(response -> {
                    HttpStatus status = (HttpStatus) response.statusCode();
                    log.info("Got response from supplier {}", response);
                    log.info("status code {}", status);

                    String lastModifiedHeader =
                            response.headers().asHttpHeaders().getFirst(HttpHeaders.LAST_MODIFIED);

                    Instant newLastModified = null;
                    if (lastModifiedHeader != null) {
                        try {
                            newLastModified = Instant.from(
                                    DateTimeFormatter.RFC_1123_DATE_TIME.parse(lastModifiedHeader)
                            );
                        } catch (Exception ex) {
                            log.warn("Failed to parse Last-Modified header: {}", lastModifiedHeader);
                        }
                    }

                    // Save lastModified in state
                    if (newLastModified != null) {
                        state.setLastModifiedAt(newLastModified);
                    }

                    // 304 Not Modified
                    if (status == HttpStatus.NOT_MODIFIED) {
                        log.info("Supplier {} returned 304 - no changes", supplierId());
                        return Mono.just(
                                PollResult.notModified(supplierId(), config.getSupplierType())
                        );
                    }

                    // 200 OK - new data
                    if (status == HttpStatus.OK) {
                        Instant finalNewLastModified = newLastModified;
                        return parseResponseBody(response, null)
                                .map(result -> {
                                    // Store lastModified in poll result
                                    result.setPolledAt(finalNewLastModified);
                                    return result;
                                });
                    }

                    // Error statuses
                    log.error("Supplier {} returned error status: {}", supplierId(), status);
                    return Mono.just(PollResult.failure(
                            supplierId(),
                            config.getSupplierType(),
                            "HTTP " + status.value()
                    ));
                })
                .onErrorResume(ex -> {
                    log.error("Polling failed for supplier: {}", supplierId(), ex);
                    return Mono.just(PollResult.failure(
                            supplierId(),
                            config.getSupplierType(),
                            ex.getMessage()
                    ));
                });
    }


    /**
     * Subclasses implement this to parse supplier-specific response format
     */
    protected abstract Mono<PollResult> parseResponseBody(ClientResponse response, String etag);
}