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
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class BusSupplierPoller extends BaseReactivePoller {

    private static final Logger log = LoggerFactory.getLogger(BusSupplierPoller.class);

    public BusSupplierPoller(WebClient.Builder webClientBuilder, PollingConfig config) {
        super(webClientBuilder, config);
    }

    @Override
    public Mono<PollResult> poll(SupplierState state) {
        String path = "/api/v1/buses/routes";

        log.debug("Polling bus supplier: {} at path: {}", supplierId(), path);

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

                    JsonNode routes = body.get("routes");
                    if (routes != null && routes.isArray()) {
                        routes.forEach(route -> {
                            TransportInventoryItem item = new TransportInventoryItem();
                            item.setId(route.get("id").asText());
                            item.setType(SupplierType.BUS);
                            item.setOrigin(route.get("fromLocation").asText());
                            item.setDestination(route.get("toLocation").asText());
                            item.setDepartureTime(Instant.parse(route.get("departureTime").asText()));
                            item.setArrivalTime(Instant.parse(route.get("arrivalTime").asText()));
                            item.setPrice(new BigDecimal(route.get("price").asText()));
                            //item.setSeatsAvailable(route.get("seatsAvailable").asInt());
                            item.setSupplierRef(route.get("ref").asText());
                            item.setUpdatedAt(Instant.now());
                            items.add(item);
                        });
                    }
                    log.info("Parsed {} bus routes from supplier: {}", items.size(), supplierId());

                    return PollResult.success(
                            supplierId(),
                            SupplierType.BUS,
                            items,
                            etag
                    );
                })
                .onErrorResume(ex -> {
                    log.error("Failed to parse bus response", ex);
                    return Mono.just(PollResult.failure(
                            supplierId(),
                            SupplierType.BUS,
                            "Parse error: " + ex.getMessage()
                    ));
                });
    }
}