package com.mmtext.supplierpollingservice.poller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mmtext.supplierpollingservice.config.PollingConfig;
import com.mmtext.supplierpollingservice.domain.PollResult;
import com.mmtext.supplierpollingservice.domain.SupplierState;
import com.mmtext.supplierpollingservice.dto.InventoryItem;
import com.mmtext.supplierpollingservice.dto.TransportInventoryItem;
import com.mmtext.supplierpollingservice.enums.SupplierType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class AirlineSupplierPoller extends BaseReactivePoller {

    private static final Logger log = LoggerFactory.getLogger(AirlineSupplierPoller.class);

    public AirlineSupplierPoller(WebClient.Builder webClientBuilder, PollingConfig config) {
        super(webClientBuilder, config);
    }

    @Override
    public Mono<PollResult> poll(SupplierState state) {
        String path = "/api/v1/flights/inventory";

        log.debug("Polling airline supplier: {} at path: {}", supplierId(), path);

        return handleResponse(
                prepareConditionalGet(path, state).exchange(),
                state
        );
    }

    @Override
    protected Mono<PollResult> parseResponseBody(ClientResponse response, String etag) {
        return response.bodyToMono(JsonNode.class)
                .map(body -> {
                    List<InventoryItem> items = new ArrayList<>();

                    // Parse JSON response (structure depends on actual supplier API)
                    JsonNode flights = body.get("flights");
                    if (flights != null && flights.isArray()) {
                        flights.forEach(flight -> {
                            TransportInventoryItem item = new TransportInventoryItem();
                            item.setId(flight.get("id").asText());
                            item.setType(SupplierType.AIRLINE);
                            item.setOrigin(flight.get("fromLocation").asText());
                            item.setDestination(flight.get("toLocation").asText());
                            item.setDepartureTime(Instant.parse(flight.get("departureTime").asText()));
                            item.setArrivalTime(Instant.parse(flight.get("arrivalTime").asText()));
                            item.setPrice(new BigDecimal(flight.get("price").asText()));
                            //item.setSeatsAvailable(flight.get("seatsAvailable").asInt());
                            item.setSupplierRef(flight.get("ref").asText());
                            item.setUpdatedAt(Instant.now());
                            items.add(item);
                        });
                    }

                    log.info("Parsed {} flights from supplier: {}", items.size(), supplierId());

                    return PollResult.success(
                            supplierId(),
                            SupplierType.AIRLINE,
                            items,
                            etag
                    );
                })
                .onErrorResume(ex -> {
                    log.error("Failed to parse airline response", ex);
                    return Mono.just(PollResult.failure(
                            supplierId(),
                            SupplierType.AIRLINE,
                            "Parse error: " + ex.getMessage()
                    ));
                });
    }
}