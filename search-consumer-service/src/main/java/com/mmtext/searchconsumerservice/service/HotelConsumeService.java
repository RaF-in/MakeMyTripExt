package com.mmtext.searchconsumerservice.service;

import com.fasterxml.jackson.databind.JsonNode;


import com.mmtext.searchconsumerservice.esdocument.AddressDocument;
import com.mmtext.searchconsumerservice.esdocument.HotelDocument;
import com.mmtext.searchconsumerservice.esdocument.RoomTypeDocument;
import com.mmtext.searchconsumerservice.repo.HotelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Service for consuming and processing Hotel events from Kafka.
 * Handles transformation from database entities to Elasticsearch documents.
 */

public class HotelConsumeService {

    private static final Logger log = LoggerFactory.getLogger(HotelConsumeService.class);
    private final HotelRepository hotelRepository;

    public HotelConsumeService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    /**
     * Save or update a hotel document in Elasticsearch.
     * Transforms the incoming data and calculates denormalized fields.
     *
     * @param hotelDocument The hotel document to save or update
     */
    public void saveOrUpdateHotel(HotelDocument hotelDocument) {
        try {
            // Enrich the document with computed/denormalized fields
            enrichHotelDocument(hotelDocument);

            // Save the document to Elasticsearch (save handles both create and update if ID exists)
            hotelRepository.save(hotelDocument);

            log.info("Successfully saved/updated HotelDocument with ID: {} and name: {}",
                    hotelDocument.getId(), hotelDocument.getName());

        } catch (Exception e) {
            log.error("Error saving hotel document with ID: {}",
                    hotelDocument != null ? hotelDocument.getId() : "null", e);
            throw new RuntimeException("Failed to save hotel document", e);
        }
    }

    /**
     * Delete a hotel document from Elasticsearch.
     *
     * @param hotelDataJson The JSON node containing the hotel data before deletion
     */
    public void deleteHotel(JsonNode hotelDataJson) {
        try {
            // Extract the ID from the 'before' state of the deleted record
            if (hotelDataJson.has("id")) {
                String hotelId = hotelDataJson.get("id").asText();
                hotelRepository.deleteById(hotelId);
                log.info("Successfully deleted HotelDocument with ID: {}", hotelId);
            } else {
                log.warn("Cannot delete hotel: ID field not found in 'before' payload.");
            }
        } catch (Exception e) {
            log.error("Error deleting hotel document: {}", hotelDataJson, e);
            throw new RuntimeException("Failed to delete hotel document", e);
        }
    }

    /**
     * Enrich hotel document with computed and denormalized fields for optimal search performance.
     *
     * @param hotelDocument The hotel document to enrich
     */
    private void enrichHotelDocument(HotelDocument hotelDocument) {
        // Extract flattened location fields from address
        if (hotelDocument.getAddress() != null) {
            AddressDocument address = hotelDocument.getAddress();
            hotelDocument.setCity(address.getCity());
            hotelDocument.setCountry(address.getCountry());
        }

        // Calculate room statistics if room types are present
        if (hotelDocument.getRoomTypes() != null && !hotelDocument.getRoomTypes().isEmpty()) {
            List<RoomTypeDocument> roomTypes = hotelDocument.getRoomTypes();

            // Calculate total rooms available
            int totalRooms = roomTypes.stream()
                    .mapToInt(rt -> rt.getTotalRooms() != null ? rt.getTotalRooms() : 0)
                    .sum();
            hotelDocument.setTotalRoomsAvailable(totalRooms);

            // Calculate min and max prices
            List<BigDecimal> prices = roomTypes.stream()
                    .map(RoomTypeDocument::getPricePerNight)
                    .filter(Objects::nonNull)
                    .toList();

            if (!prices.isEmpty()) {
                BigDecimal minPrice = prices.stream()
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                BigDecimal maxPrice = prices.stream()
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

                hotelDocument.setMinPrice(minPrice.doubleValue());
                hotelDocument.setMaxPrice(maxPrice.doubleValue());
            }

            // Set hotel reference in each room type for bi-directional navigation
            String hotelId = hotelDocument.getId();
            String hotelName = hotelDocument.getName();
            roomTypes.forEach(roomType -> {
                roomType.setHotelId(hotelId);
                roomType.setHotelName(hotelName);
            });
        } else {
            // Set defaults if no room types
            hotelDocument.setTotalRoomsAvailable(0);
            hotelDocument.setMinPrice(0.0);
            hotelDocument.setMaxPrice(0.0);
        }

        // Ensure amenities list is not null
        if (hotelDocument.getAmenities() == null) {
            hotelDocument.setAmenities(new ArrayList<>());
        }

        log.debug("Enriched hotel document - ID: {}, City: {}, Total Rooms: {}, Price Range: {}-{}",
                hotelDocument.getId(),
                hotelDocument.getCity(),
                hotelDocument.getTotalRoomsAvailable(),
                hotelDocument.getMinPrice(),
                hotelDocument.getMaxPrice());
    }
}