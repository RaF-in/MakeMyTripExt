package com.mmtext.supplieradapterservice.listeners;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmtext.supplieradapterservice.dto.RoomInventoryItem;
import com.mmtext.supplieradapterservice.service.RoomTypePollingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class KafkaEventListeners {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventListeners.class);

    private final RoomTypePollingService roomTypePollingService;
    private final ObjectMapper objectMapper;

    public KafkaEventListeners(RoomTypePollingService roomTypePollingService, ObjectMapper objectMapper) {
        this.roomTypePollingService = roomTypePollingService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "normalize.hotel_info", groupId = "hotel-normalize-group")
    public void listenHotelBatch(List<ConsumerRecord<String, String>> records) {
        log.info("Received batch of {} hotel messages on thread {}",
                records.size(), Thread.currentThread().getName());

        if (records.isEmpty()) {
            return;
        }

        List<RoomInventoryItem> entities = records.stream()
                .map(record -> {
                    try {
                        String jsonString = record.value();
                        RoomInventoryItem item = objectMapper.readValue(
                                jsonString,
                                RoomInventoryItem.class
                        );
                        log.debug("Successfully deserialized hotel: {}", item.getHotelId());
                        return item;
                    } catch (JsonProcessingException e) {
                        log.error("Error deserializing hotel record with key {}: {}",
                                record.key(), e.getMessage(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!entities.isEmpty()) {
            roomTypePollingService.syncRoomTypes(entities);
            log.info("Successfully saved {} hotel records", entities.size());
        }
    }
}
