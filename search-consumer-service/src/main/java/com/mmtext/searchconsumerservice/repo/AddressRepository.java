package com.mmtext.searchconsumerservice.repo;

import com.mmtext.searchconsumerservice.esdocument.AddressDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch repository for Address documents.
 * Provides geo-spatial query capabilities and location-based searches.
 */
@Repository
public interface AddressRepository extends ElasticsearchRepository<AddressDocument, String> {

    /**
     * Find addresses by city.
     */
    List<AddressDocument> findByCity(String city);

    /**
     * Find addresses by state.
     */
    List<AddressDocument> findByState(String state);

    /**
     * Find addresses by country.
     */
    Page<AddressDocument> findByCountry(String country, Pageable pageable);

    /**
     * Find addresses by city and state.
     */
    List<AddressDocument> findByCityAndState(String city, String state);

    /**
     * Find addresses by zip code.
     */
    List<AddressDocument> findByZip(String zip);

    /**
     * Find addresses within a certain distance from a point.
     * Distance should be specified like "10km" or "5mi".
     */
    @Query("{\"bool\": {\"filter\": {\"geo_distance\": {\"distance\": \"?1\", \"location\": {\"lat\": ?0.lat, \"lon\": ?0.lon}}}}}")
    List<AddressDocument> findAddressesWithinDistance(GeoPoint center, String distance);

    /**
     * Find addresses by geohash prefix for regional queries.
     */
    List<AddressDocument> findByGeohashStartingWith(String geohashPrefix);

    /**
     * Search addresses by text in street field.
     */
    @Query("{\"match\": {\"street\": \"?0\"}}")
    List<AddressDocument> searchByStreet(String streetText);

    /**
     * Find addresses in a bounding box.
     */
    @Query("{\"bool\": {\"filter\": {\"geo_bounding_box\": {\"location\": {\"top_left\": {\"lat\": ?0, \"lon\": ?1}, \"bottom_right\": {\"lat\": ?2, \"lon\": ?3}}}}}}")
    List<AddressDocument> findAddressesInBoundingBox(Double topLat, Double leftLon, Double bottomLat, Double rightLon);
}