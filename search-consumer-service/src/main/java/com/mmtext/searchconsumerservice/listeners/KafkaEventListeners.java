package com.mmtext.searchconsumerservice.listeners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventListeners {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventListeners.class);
    private final ObjectMapper objectMapper;

    public KafkaEventListeners(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    //@KafkaListener(topics = "${kafka.topic.db-movie-events}",
            //groupId = "${kafka.group.show}")
    @KafkaListener(topics = "mmtext.public.movie_outbox_events", groupId = "movie-consumer-group")
    public void listen(ConsumerRecord<String, String> record) throws Exception {
        String topic = record.topic();
        String message = record.value();

        log.info("Received message from topic {}", topic);
        log.debug("Message content: {}", message);

        try {
            // Parse the Debezium message format
            JsonNode eventNode = objectMapper.readTree(message);

            // Handle database trigger events
            if (eventNode.has("after")) {
                JsonNode afterNode = eventNode.get("after");
                if (afterNode != null && afterNode.has("type") && afterNode.has("payload")) {
                    String eventType = afterNode.get("type").asText();
                    String payloadString = afterNode.get("payload").asText();

                    // Parse the JSON inside payload
                    JsonNode movieDataJson = objectMapper.readTree(payloadString);

                    log.info("Processing database trigger event: {}", eventType);
                    log.info("Movie data: {}", movieDataJson.toString());
                }

            }
        } catch (Exception e) {
            log.error("Error processing message from topic {}: {}", topic, e.getMessage(), e);
            throw e; // Let Kafka handle retry logic
        }
    }

    @KafkaListener(topics = "${kafka.topic.db-movie-events}",
            groupId = "${kafka.group.movie}")
    public void listenRaw(String message) {
        System.out.println("RAW MESSAGE: " + message);
    }

}
