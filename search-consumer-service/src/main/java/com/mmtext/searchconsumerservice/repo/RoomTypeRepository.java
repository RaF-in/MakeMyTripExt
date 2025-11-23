package com.mmtext.searchconsumerservice.repo;

import com.mmtext.searchconsumerservice.esdocument.RoomTypeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Elasticsearch repository for RoomType documents.
 * Provides search capabilities for room availability, pricing, and facilities.
 */
@Repository
public interface RoomTypeRepository extends ElasticsearchRepository<RoomTypeDocument, String> {

    /**
     * Find room types by hotel ID.
     */
    List<RoomTypeDocument> findByHotelId(String hotelId);

    /**
     * Find room types by room type name.
     */
    List<RoomTypeDocument> findByRoomType(String roomType);

    /**
     * Find room types by reference.
     */
    RoomTypeDocument findByRef(String ref);

    /**
     * Find room types with price less than or equal to specified amount.
     */
    List<RoomTypeDocument> findByPricePerNightLessThanEqual(BigDecimal maxPrice);

    /**
     * Find room types within a price range.
     */
    List<RoomTypeDocument> findByPricePerNightBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find room types with specific facility.
     */
    List<RoomTypeDocument> findByFacilitiesContaining(String facility);

    /**
     * Find room types with all specified facilities.
     */
    @Query("{\"bool\": {\"must\": [{\"terms\": {\"facilities\": ?0}}]}}")
    List<RoomTypeDocument> findByAllFacilities(List<String> facilities);

    /**
     * Find room types with minimum number of rooms available.
     */
    List<RoomTypeDocument> findByTotalRoomsGreaterThanEqual(Integer minRooms);

    /**
     * Find available room types by hotel and price range.
     */
    List<RoomTypeDocument> findByHotelIdAndPricePerNightBetween(
            String hotelId, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Search room types with flexible matching on room type name.
     */
    @Query("{\"match\": {\"roomType\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}")
    List<RoomTypeDocument> searchByRoomTypeFuzzy(String roomType);

    /**
     * Find cheapest rooms with pagination.
     */
    Page<RoomTypeDocument> findByPricePerNightLessThanEqualOrderByPricePerNightAsc(
            BigDecimal maxPrice, Pageable pageable);

    /**
     * Count room types by hotel.
     */
    Long countByHotelId(String hotelId);

    /**
     * Find room types by hotel name (denormalized field).
     */
    @Query("{\"match\": {\"hotelName\": \"?0\"}}")
    List<RoomTypeDocument> findByHotelName(String hotelName);
}