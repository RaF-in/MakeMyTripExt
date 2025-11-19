package com.mmtext.supplierpollingservice.poller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mmtext.supplierpollingservice.config.PollingConfig;
import com.mmtext.supplierpollingservice.domain.PollResult;
import com.mmtext.supplierpollingservice.domain.SupplierState;
import com.mmtext.supplierpollingservice.dto.InventoryItem;
import com.mmtext.supplierpollingservice.dto.RoomInventoryItem;
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
public class HotelSupplierPoller extends BaseReactivePoller {

    private static final Logger log = LoggerFactory.getLogger(HotelSupplierPoller.class);

    public HotelSupplierPoller(WebClient.Builder webClientBuilder, PollingConfig config) {
        super(webClientBuilder, config);
    }

    @Override
    public Mono<PollResult> poll(SupplierState state) {
        String path = buildPathWithCursor("/api/admin/hotel", state.getCursor());

        log.debug("Polling hotel supplier: {} at path: {}", supplierId(), path);

        return handleResponse(
                prepareConditionalGet(path, state).exchange(),
                state
        );
    }

    private String buildPathWithCursor(String basePath, String cursor) {
        if (config.isSupportsPagination() && cursor != null) {
            return basePath + "?cursor=" + cursor;
        }
        return basePath;
    }

    @Override
    protected Mono<PollResult> parseResponseBody(ClientResponse response, String etag) {
        return response.bodyToMono(JsonNode.class)
                .map(body -> {
                    List<InventoryItem> items = new ArrayList<>();
                    String nextCursor = null;

                    if (body.isArray()) {
                        for (JsonNode element : body) {
                            JsonNode hotel = element.get("hotel");
                            JsonNode roomTypeNodes = hotel.get("roomTypes");
                            log.info("roomType nodes are {}",roomTypeNodes.toString());
                            log.info("Hotels {}  received at poller", hotel);
                            RoomInventoryItem item = new RoomInventoryItem();
                            for (JsonNode roomTypeNode : roomTypeNodes) {
                                item.setId(hotel.get("id").asText());
                                item.setType(SupplierType.HOTEL);
                                //item.setOrigin(hotel.get("city").asText());
                                //item.setDestination(hotel.get("city").asText());
                                //item.setDepartureTime(Instant.parse(hotel.get("departure").asText()));
                                item.setPrice(new BigDecimal(roomTypeNode.get("pricePerNight").asText()));
                                //item.setSeatsAvailable(hotel.get("roomsAvailable").asInt());
                                item.setSupplierRef(hotel.get("ref").asText());
                                item.setUpdatedAt(Instant.now());
                                item.setRoomType(roomTypeNode.get("roomType").asText());
                                item.setHotelId(hotel.get("id").asText());

                                items.add(item);
                            }
                        }
                    }

                    // Extract pagination cursor for next poll
                    JsonNode pagination = body.get("pagination");
                    if (pagination != null && pagination.has("nextCursor")) {
                        nextCursor = pagination.get("nextCursor").asText();
                    }

                    log.info("Parsed {} hotels from supplier: {}, nextCursor: {}",
                            items.size(), supplierId(), nextCursor);
                    log.info("item are {}", items);

                    PollResult result = PollResult.success(
                            supplierId(),
                            SupplierType.HOTEL,
                            items,
                            etag
                    );
                    result.setNewCursor(nextCursor);
                    return result;
                })
                .onErrorResume(ex -> {
                    log.error("Failed to parse hotel response", ex);
                    return Mono.just(PollResult.failure(
                            supplierId(),
                            SupplierType.HOTEL,
                            "Parse error: " + ex.getMessage()
                    ));
                });
    }
}