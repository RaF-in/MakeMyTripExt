package com.mmtext.searchconsumerservice.listeners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmtext.searchconsumerservice.esdocument.MovieDocument;

import com.mmtext.searchconsumerservice.repo.MovieSearchRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Good practice for transactional operations if needed

@Service
public class KafkaEventListeners {
    private static final Logger log = LoggerFactory.getLogger(KafkaEventListeners.class);
    private final ObjectMapper objectMapper;
    private final MovieSearchRepository movieSearchRepository;

    public KafkaEventListeners(ObjectMapper objectMapper, MovieSearchRepository movieSearchRepository) {
        this.objectMapper = objectMapper;
        this.movieSearchRepository = movieSearchRepository;
    }

    @KafkaListener(topics = "mmtext.public.movie_outbox_events", groupId = "movie-consumer-group")
    public void listen(ConsumerRecord<String, String> record) throws Exception {
        String message = record.value();

        try {
            JsonNode eventNode = objectMapper.readTree(message);
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
                        saveOrUpdateMovie(afterNode);
                    }
                    break;
                case "d": // Delete
                    // For delete operations, we use the 'before' state to get the ID
                    JsonNode beforeNode = payloadNode.get("before");
                    if (beforeNode != null) {
                        deleteMovie(beforeNode);
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

    private void saveOrUpdateMovie(JsonNode movieDataJson) {
        try {
            // Deserialize the JSON node directly into the MovieDocument class
//            MovieDocument movieDocument = objectMapper.treeToValue(movieDataJson, MovieDocument.class);

            // Save the document to Elasticsearch (save handles both create and update if ID exists)
//            movieSearchRepository.save(movieDocument);

//            log.info("Successfully saved/updated MovieDocument with ID: {}", movieDocument.getId());

        } catch (Exception e) {
            log.error("Error saving movie document: {}", movieDataJson.toString(), e);
            // Handle error appropriately
        }
    }

    private void deleteMovie(JsonNode movieDataJson) {
        // Extract the ID from the 'before' state of the deleted record
        if (movieDataJson.has("id")) {
            Long movieId = movieDataJson.get("id").asLong();
//            movieSearchRepository.deleteById(movieId);
            log.info("Successfully deleted MovieDocument with ID: {}", movieId);
        } else {
            log.warn("Cannot delete movie: ID field not found in 'before' payload.");
        }
    }
}
