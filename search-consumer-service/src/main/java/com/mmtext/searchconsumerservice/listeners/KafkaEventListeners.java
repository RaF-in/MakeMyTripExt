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

    @KafkaListener(topics = "${kafka.topic.movie-events}",
            groupId = "${kafka.group.movie}")
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
                if (afterNode.has("type")) {
                    String eventType = afterNode.get("type").asText();

                    if (("CREATED".equals(eventType) ||
                            "UPDATED".equals(eventType) ||
                            "DELETED".equals(eventType)) &&
                            afterNode.has("payload")) {

                        JsonNode movieDataJson = objectMapper.readTree(
                                afterNode.get("payload").asText());
                        log.info("Processing database trigger event: {}", eventType);
                        log.info("Movie data: {}", movieDataJson.asText());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing message from topic {}: {}", topic, e.getMessage(), e);
            throw e; // Let Kafka handle retry logic
        }
    }
}
