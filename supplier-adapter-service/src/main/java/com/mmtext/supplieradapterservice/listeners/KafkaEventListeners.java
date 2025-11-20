package com.mmtext.supplieradapterservice.listeners;
import com.mmtext.supplieradapterservice.dto.RoomInventoryItem;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KafkaEventListeners {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventListeners.class);

    @KafkaListener(topics = "normalize.hotel_info", groupId = "hotel-normalize-group")
    public void listenBatch(List<ConsumerRecord<String, RoomInventoryItem>> records) {

        log.info("Received batch of {} messages on thread {}",
                records.size(), Thread.currentThread().getName());

        if (records.isEmpty()) {
            return;
        }
//        List<RoomInventoryEntity> entities = records.stream()
//                .map(record -> {
//                    try {
//                        RoomInventoryItem item = record.value();
//                        return normalizeAndConvert(item);
//                    } catch (Exception e) {
//                        log.error("Error processing message: {}", e.getMessage(), e);
//                        return null;
//                    }
//                })
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//
//        // Single batch insert/update
//        repository.saveAll(entities);
//        log.info("Successfully saved {} records", entities.size());
    }

//    private RoomInventoryEntity normalizeAndConvert(RoomInventoryItem item) {
//        // Your normalization logic here
//        RoomInventoryEntity entity = new RoomInventoryEntity();
//        entity.setHotelId(item.getHotelId());
//        entity.setPrice(item.getPrice());
//        entity.setRoomType(item.getRoomType());
//        entity.setSupplierRef(item.getSupplierRef());
//        entity.setUpdatedAt(item.getUpdatedAt());
//        return entity;
//    }
}
