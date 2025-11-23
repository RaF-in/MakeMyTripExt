package com.mmtext.searchconsumerservice.repo;

import com.mmtext.searchconsumerservice.esdocument.HotelDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch repository for Hotel documents.
 * Provides comprehensive search capabilities including full-text search,
 * filtering by amenities, location, rating, and price range.
 */
@Repository
public interface HotelRepository extends ElasticsearchRepository<HotelDocument, String> {

    /**
     * Find hotels by name with exact match.
     */
    List<HotelDocument> findByName(String name);

    /**
     * Find hotel by reference.
     */
    HotelDocument findByRef(String ref);

    /**
     * Find hotel by supplier reference.
     */
    HotelDocument findBySupplierRef(String supplierRef);

    /**
     * Find hotels by city.
     */
    Page<HotelDocument> findByCity(String city, Pageable pageable);

    /**
     * Find hotels by country.
     */
    Page<HotelDocument> findByCountry(String country, Pageable pageable);

    /**
     * Find hotels by city and country.
     */
    List<HotelDocument> findByCityAndCountry(String city, String country);

    /**
     * Find hotels with rating greater than or equal to specified value.
     */
    Page<HotelDocument> findByRatingGreaterThanEqual(Double minRating, Pageable pageable);

    /**
     * Find hotels with specific amenity.
     */
    List<HotelDocument> findByAmenitiesContaining(String amenity);

    /**
     * Find hotels with all specified amenities.
     */
    @Query("{\"bool\": {\"must\": [{\"terms\": {\"amenities\": ?0}}]}}")
    List<HotelDocument> findByAllAmenities(List<String> amenities);

    /**
     * Find hotels within a price range.
     */
    List<HotelDocument> findByMinPriceLessThanEqualAndMaxPriceGreaterThanEqual(
            Double maxBudget, Double minBudget);

    /**
     * Full-text search on hotel name and description.
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^2\", \"description\"], \"fuzziness\": \"AUTO\"}}")
    Page<HotelDocument> searchByNameAndDescription(String searchText, Pageable pageable);

    /**
     * Find hotels by city with rating filter.
     */
    Page<HotelDocument> findByCityAndRatingGreaterThanEqual(
            String city, Double minRating, Pageable pageable);

    /**
     * Find hotels with nested query on address location.
     */
    @Query("{\"nested\": {\"path\": \"address\", \"query\": {\"bool\": {\"must\": [{\"match\": {\"address.city\": \"?0\"}}]}}}}")
    List<HotelDocument> findByNestedAddressCity(String city);

    /**
     * Complex search: city, rating, amenities, and price range.
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"city\": \"?0\"}}, {\"range\": {\"rating\": {\"gte\": ?1}}}, " +
            "{\"range\": {\"minPrice\": {\"lte\": ?2}}}, {\"terms\": {\"amenities\": ?3}}]}}")
    Page<HotelDocument> advancedSearch(String city, Double minRating, Double maxPrice,
                                       List<String> amenities, Pageable pageable);

    /**
     * Find hotels near a location (using nested geo query on address).
     */
    @Query("{\"nested\": {\"path\": \"address\", \"query\": {\"bool\": {\"filter\": " +
            "{\"geo_distance\": {\"distance\": \"?1\", \"address.location\": {\"lat\": ?0.lat, \"lon\": ?0.lon}}}}}}}")
    List<HotelDocument> findHotelsNearLocation(String locationGeoJson, String distance);

    /**
     * Search hotels with availability (rooms available).
     */
    List<HotelDocument> findByTotalRoomsAvailableGreaterThan(Integer minRooms);

    /**
     * Find top-rated hotels with pagination.
     */
    Page<HotelDocument> findByOrderByRatingDesc(Pageable pageable);

    /**
     * Count hotels by city.
     */
    Long countByCity(String city);

    /**
     * Find budget hotels (below certain price).
     */
    Page<HotelDocument> findByMaxPriceLessThanEqualOrderByMinPriceAsc(
            Double maxPrice, Pageable pageable);
}