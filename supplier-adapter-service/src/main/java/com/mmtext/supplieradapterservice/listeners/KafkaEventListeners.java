package com.mmtext.supplieradapterservice.listeners;
import com.mmtext.supplieradapterservice.dto.RoomInventoryItem;
import com.mmtext.supplieradapterservice.service.RoomTypePollingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class KafkaEventListeners {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventListeners.class);

    private final RoomTypePollingService roomTypePollingService;

    public KafkaEventListeners(RoomTypePollingService roomTypePollingService) {
        this.roomTypePollingService = roomTypePollingService;
    }

    @KafkaListener(topics = "normalize.hotel_info", groupId = "hotel-normalize-group")
    public void listenBatch(List<ConsumerRecord<String, RoomInventoryItem>> records) {

        log.info("Received batch of {} messages on thread {}",
                records.size(), Thread.currentThread().getName());

        if (records.isEmpty()) {
            return;
        }
        List<RoomInventoryItem> entities = records.stream()
                .map(record -> {
                    try {
                        RoomInventoryItem item = record.value();
                        return item;
                    } catch (Exception e) {
                        log.error("Error processing message: {}", e.getMessage(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        roomTypePollingService.syncRoomTypes(entities);
        log.info("Successfully saved {} records", entities.size());
    }
}
