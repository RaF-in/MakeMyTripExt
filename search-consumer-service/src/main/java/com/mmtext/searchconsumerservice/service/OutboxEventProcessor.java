package com.mmtext.searchconsumerservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mmtext.searchconsumerservice.interfaces.OutboxEventHandler;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic processor for outbox event patterns from Kafka topics.
 * Handles CDC (Change Data Capture) events with create, update, and delete operations.
 *
 * @param <T> The document type to be processed
 */

public class OutboxEventProcessor<T> {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventProcessor.class);
    private final ObjectMapper objectMapper;
    private final Class<T> documentClass;
    private final OutboxEventHandler<T> eventHandler;

    public OutboxEventProcessor(ObjectMapper objectMapper,
                                Class<T> documentClass,
                                OutboxEventHandler<T> eventHandler) {
        this.objectMapper = objectMapper;
        this.documentClass = documentClass;
        this.eventHandler = eventHandler;
    }

    /**
     * Process a Kafka consumer record containing an outbox event.
     *
     * @param record The Kafka consumer record
     * @throws Exception if processing fails
     */
    public void process(ConsumerRecord<String, String> record) throws Exception {
        String topic = record.topic();
        String message = record.value();

        log.info("Received message from topic {}", topic);
        log.debug("Message content: {}", message);

        try {
            JsonNode eventNode = objectMapper.readTree(message);
            log.warn("Received message {}", eventNode.toString());

            if (!eventNode.has("after")) {
                log.warn("Message missing 'after' field, skipping.");
                return;
            }

            JsonNode afterNode = eventNode.get("after");
            if (afterNode == null || !afterNode.has("type") || !afterNode.has("payload")) {
                log.warn("Invalid 'after' node structure, skipping.");
                return;
            }

            String eventType = afterNode.get("type").asText();
            JsonNode payloadNode = afterNode.get("payload");

            if (payloadNode == null) {
                log.warn("Received message without expected 'payload' field, skipping.");
                return;
            }

            String payloadString = payloadNode.asText();
            String operation = eventNode.get("op").asText();

            log.info("Processing database operation: {}", operation);
            log.info("Processing database trigger event: {}", eventType);

            JsonNode dataJson = objectMapper.readTree(payloadString);
            log.info("{} data: {}", documentClass.getSimpleName(), dataJson.toString());

            handleOperation(operation, eventNode, dataJson);

        } catch (Exception e) {
            log.error("Error processing message from topic {}: {}", topic, e.getMessage(), e);
            throw e; // Let Kafka retry if needed
        }
    }

    private void handleOperation(String operation, JsonNode eventNode, JsonNode dataJson) throws Exception {
        switch (operation) {
            case "c": // Create
            case "u": // Update
                T document = objectMapper.treeToValue(dataJson, documentClass);
                eventHandler.saveOrUpdate(document);
                break;
            case "d": // Delete
                JsonNode beforeNode = eventNode.get("before");
                if (beforeNode != null) {
                    eventHandler.delete(beforeNode);
                } else {
                    log.warn("Delete operation received but 'before' node is missing");
                }
                break;
            default:
                log.info("Unknown or unsupported operation type: {}", operation);
                break;
        }
    }
}