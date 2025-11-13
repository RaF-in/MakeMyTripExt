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
import org.springframework.transaction.annotation.Transactional; // Good practice for transactional operations if needed

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
        String message = record.value();

        try {
            JsonNode eventNode = objectMapper.readTree(message);
            log.warn(eventNode.toString());
            // The actual Debezium data is often nested under a 'payload' key
            JsonNode payloadNode = eventNode.get("payload");

            if (payloadNode == null) {
                log.warn("Received message without expected 'payload' field, skipping.");
                return;
            }

            // Determine the operation type: 'c' (create), 'u' (update), or 'd' (delete)
            String operation = payloadNode.get("op").asText();

            log.info("Processing database operation: {}", operation);

            switch (operation) {
                case "c": // Create
                case "u": // Update
                    // For create and update operations, we use the 'after' state
                    JsonNode afterNode = payloadNode.get("after");
                    if (afterNode != null) {
                        movieConsumeService.saveOrUpdateMovie(objectMapper.treeToValue(afterNode, MovieDocument.class));
                    }
                    break;
                case "d": // Delete
                    // For delete operations, we use the 'before' state to get the ID
                    JsonNode beforeNode = payloadNode.get("before");
                    if (beforeNode != null) {
                        movieConsumeService.deleteMovie(beforeNode);
                    }
                    break;
                default:
                    log.info("Unknown or unsupported operation type: {}", operation);
                    break;
            }

        } catch (Exception e) {
            log.error("Error processing message from topic {}: {}", record.topic(), e.getMessage(), e);
            throw e; // Let Kafka retry if needed
        }
    }
}
