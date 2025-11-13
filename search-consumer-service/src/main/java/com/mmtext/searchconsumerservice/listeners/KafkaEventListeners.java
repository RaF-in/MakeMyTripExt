package com.mmtext.searchconsumerservice.listeners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmtext.searchconsumerservice.esdocument.MovieDocument;
import com.mmtext.searchconsumerservice.service.MovieConsumeService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventListeners {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventListeners.class);

    private final ObjectMapper objectMapper;

    @Autowired
    MovieConsumeService movieConsumeService;

    public KafkaEventListeners(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "mmtext.public.movie_outbox_events", groupId = "movie-consumer-group")
    public void listen(ConsumerRecord<String, String> record) throws Exception {
        String topic = record.topic();
        String message = record.value();

        log.info("Received message from topic {}", topic);
        log.debug("Message content: {}", message);

        try {
            JsonNode eventNode = objectMapper.readTree(message);
            log.warn("Received message {}", eventNode.toString());
            if (eventNode.has("after")) {
                JsonNode afterNode = eventNode.get("after");
                if (afterNode != null && afterNode.has("type") && afterNode.has("payload")) {
                    String eventType = afterNode.get("type").asText();
                    String payloadString = afterNode.get("payload").asText();
                    JsonNode payloadNode = afterNode.get("payload");
                    if (payloadNode == null) {
                        log.warn("Received message without expected 'payload' field, skipping.");
                        return;
                    }
                    String operation = eventNode.get("op").asText();
                    log.info("Processing database operation: {}", operation);
                    JsonNode movieDataJson = objectMapper.readTree(payloadString);

                    log.info("Processing database trigger event: {}", eventType);
                    log.info("Movie data: {}", movieDataJson.toString());
                    // Parse the JSON inside payload
                    switch (operation) {
                        case "c": // Create
                        case "u": // Update
                            movieConsumeService.saveOrUpdateMovie(objectMapper.treeToValue(movieDataJson, MovieDocument.class));
                            break;
                        case "d": // Delete
                            // For delete operations, we use the 'before' state to get the ID
                            JsonNode beforeNode = eventNode.get("before");
                            if (beforeNode != null) {
                                movieConsumeService.deleteMovie(beforeNode);
                            }
                            break;
                        default:
                            log.info("Unknown or unsupported operation type: {}", operation);
                            break;
                    }

                }

            }
        } catch (Exception e) {
            log.error("Error processing message from topic {}: {}", record.topic(), e.getMessage(), e);
            throw e; // Let Kafka retry if needed
        }
    }
}
