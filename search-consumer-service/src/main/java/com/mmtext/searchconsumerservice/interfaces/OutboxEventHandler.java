package com.mmtext.searchconsumerservice.interfaces;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interface for handling outbox events.
 * Implement this interface for each document type that needs to be processed.
 *
 * @param <T> The document type
 */
public interface OutboxEventHandler<T> {

    /**
     * Save or update the document.
     *
     * @param document The document to save or update
     */
    void saveOrUpdate(T document);

    /**
     * Delete the document using data from the 'before' node.
     *
     * @param beforeNode The JSON node containing the document state before deletion
     */
    void delete(JsonNode beforeNode);
}