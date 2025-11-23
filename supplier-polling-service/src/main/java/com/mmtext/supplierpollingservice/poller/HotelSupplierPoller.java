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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class HotelSupplierPoller extends BaseReactivePoller {

    private static final Logger log = LoggerFactory.getLogger(HotelSupplierPoller.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public HotelSupplierPoller(WebClient.Builder webClientBuilder, PollingConfig config, KafkaTemplate<String, Object> kafkaTemplate) {
        super(webClientBuilder, config);
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Mono<PollResult> poll(SupplierState state) {
        String path = buildPathWithCursor("/api/admin/roomType", state.getCursor());

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
                            JsonNode hotelId = element.get("hotelId");
                            JsonNode roomTypeNode = element.get("roomType");
                            JsonNode hotelRefNode = element.get("hotelRef");
                            JsonNode supplierRefNode = element.get("supplierRef");
                            log.info("roomType nodes are {}",roomTypeNode.toString());
                            log.info("Hotel id {}  received at poller", hotelId);
                            RoomInventoryItem item = new RoomInventoryItem();
                            item.setType(SupplierType.HOTEL);
                            item.setPrice(new BigDecimal(roomTypeNode.get("pricePerNight").asText()));
                            item.setSupplierRef(supplierRefNode.asText());
                            item.setHotelRef(hotelRefNode.asText());
                            item.setUpdatedAt(Instant.now());
                            item.setRoomType(roomTypeNode.get("roomType").asText());
                            item.setSeatsAvailable(roomTypeNode.get("totalRooms").asInt());
                            item.setHotelId(hotelId.asText());
                            item.setHotelName(element.get("hotelName").asText());
                            item.setDescription(element.get("description").asText());
                            item.setRating(element.get("rating").asDouble());
                            kafkaTemplate.send("normalize.hotel_info", item.getHotelId(), item);
                            items.add(item);
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