package com.mmtext.searchservice.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import com.mmtext.searchservice.esdocument.HotelDocument;
import com.mmtext.searchservice.esdocument.RoomTypeDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for hotel search operations using Elasticsearch.
 * Provides comprehensive search capabilities for OTA (Online Travel Agency) systems.
 */

@Service

public class HotelSearchService {

    private static final Logger log = LoggerFactory.getLogger(HotelSearchService.class);
    private final ElasticsearchOperations elasticsearchOperations;

    public HotelSearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    /**
     * 🔍 Search hotels by destination (city or country)
     */
    @Cacheable(value = "hotels", key = "'destination-' + #location + '-' + #checkIn + '-' + #checkOut + '-' + #guests + '-' + #page")
    public List<HotelDocument> searchByDestination(String location, LocalDate checkIn,
                                                   LocalDate checkOut, int guests, int page, int size) {
        log.info("Searching hotels in destination: {} for {} guests", location, guests);

        Query locationQuery = Query.of(q -> q
                .bool(b -> b
                        .should(s1 -> s1.match(m -> m.field("city").query(location)))
                        .should(s2 -> s2.match(m -> m.field("country").query(location)))
                        .minimumShouldMatch("1")
                )
        );

        // Filter by minimum rooms available based on guests
        Query availabilityQuery = Query.of(q -> q
                .range(r -> r
                        .number(n -> n
                                .field("totalRoomsAvailable")
                                .gte((double) Math.ceil(guests / 2.0))
                        )
                )
        );

        Query boolQuery = Query.of(q -> q
                .bool(b -> b
                        .must(locationQuery)
                        .filter(availabilityQuery)
                )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size))
                .withSort(s -> s.field(f -> f.field("rating").order(SortOrder.Desc)))
                .build();

        return elasticsearchOperations.search(nativeQuery, HotelDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 📍 Search hotels near a location using geo-spatial query
     */
    @Cacheable(value = "hotels", key = "'nearby-' + #lat + '-' + #lon + '-' + #radius + '-' + #guests + '-' + #page")
    public List<HotelDocument> searchHotelsNearby(double lat, double lon, String radius,
                                                  int guests, int page, int size) {
        log.info("Searching hotels near location: {}, {} within radius: {}", lat, lon, radius);

        Query geoQuery = Query.of(q -> q
                .nested(n -> n
                        .path("address")
                        .query(nq -> nq
                                .geoDistance(g -> g
                                        .field("address.location")
                                        .location(l -> l.latlon(ll -> ll.lat(lat).lon(lon)))
                                        .distance(radius)
                                )
                        )
                )
        );

        Query availabilityQuery = Query.of(q -> q
                .range(r -> r
                        .number(n -> n
                                .field("totalRoomsAvailable")
                                .gte((double) Math.ceil(guests / 2.0))
                        )
                )
        );

        Query boolQuery = Query.of(q -> q
                .bool(b -> b
                        .must(geoQuery)
                        .filter(availabilityQuery)
                )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size))
                .build();

        return elasticsearchOperations.search(nativeQuery, HotelDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 💰 Filter hotels by price range
     */
    @Cacheable(value = "hotels", key = "'price-' + #minPrice + '-' + #maxPrice + '-' + #city + '-' + #page")
    public List<HotelDocument> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice,
                                                  String city, int page, int size) {
        log.info("Searching hotels with price range: {} - {}", minPrice, maxPrice);

        List<Query> mustQueries = new ArrayList<>();

        // Price range filter
        mustQueries.add(Query.of(q -> q
                .range(r -> r
                        .number(n -> n
                                .field("minPrice")
                                .gte(minPrice.doubleValue())
                                .lte(maxPrice.doubleValue())
                        )
                )
        ));

        // Optional city filter
        if (city != null && !city.isEmpty()) {
            mustQueries.add(Query.of(q -> q
                    .term(t -> t.field("city.keyword").value(city))
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(b -> b.must(mustQueries)));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size))
                .withSort(s -> s.field(f -> f.field("minPrice").order(SortOrder.Asc)))
                .build();

        return elasticsearchOperations.search(nativeQuery, HotelDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * ⭐ Filter hotels by minimum rating
     */
    @Cacheable(value = "hotels", key = "'rating-' + #minRating + '-' + #city + '-' + #page")
    public List<HotelDocument> searchByRating(Double minRating, String city, int page, int size) {
        log.info("Searching hotels with minimum rating: {}", minRating);

        List<Query> mustQueries = new ArrayList<>();

        mustQueries.add(Query.of(q -> q
                .range(r -> r
                        .number(n -> n.field("rating").gte(minRating))
                )
        ));

        if (city != null && !city.isEmpty()) {
            mustQueries.add(Query.of(q -> q
                    .term(t -> t.field("city.keyword").value(city))
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(b -> b.must(mustQueries)));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size))
                .withSort(s -> s.field(f -> f.field("rating").order(SortOrder.Desc)))
                .build();

        return elasticsearchOperations.search(nativeQuery, HotelDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 🎯 Advanced hotel search with multiple filters
     */
    @Cacheable(value = "hotels",
            key = "'advanced-' + #location + '-' + #minPrice + '-' + #maxPrice + '-' + #minRating + '-' + #amenities + '-' + #sortBy + '-' + #order + '-' + #page")
    public List<HotelDocument> advancedHotelSearch(String location, BigDecimal minPrice,
                                                   BigDecimal maxPrice, Double minRating,
                                                   List<String> amenities, String sortBy,
                                                   String order, int page, int size) {
        log.info("Advanced hotel search with filters - location: {}, price: {}-{}, rating: {}",
                location, minPrice, maxPrice, minRating);

        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        // Location filter
        if (location != null && !location.isEmpty()) {
            mustQueries.add(Query.of(q -> q
                    .bool(b -> b
                            .should(s1 -> s1.match(m -> m.field("city").query(location)))
                            .should(s2 -> s2.match(m -> m.field("country").query(location)))
                            .minimumShouldMatch("1")
                    )
            ));
        }

        // Price range filter
        if (minPrice != null && maxPrice != null) {
            filterQueries.add(Query.of(q -> q
                    .range(r -> r
                            .number(n -> n
                                    .field("minPrice")
                                    .gte(minPrice.doubleValue())
                                    .lte(maxPrice.doubleValue())
                            )
                    )
            ));
        }

        // Rating filter
        if (minRating != null) {
            filterQueries.add(Query.of(q -> q
                    .range(r -> r.number(n -> n.field("rating").gte(minRating)))
            ));
        }

        // Amenities filter
        if (amenities != null && !amenities.isEmpty()) {
            List<FieldValue> amenityValues = amenities.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());

            filterQueries.add(Query.of(q -> q
                    .terms(t -> t.field("amenities").terms(tv -> tv.value(amenityValues)))
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(b -> {
            if (!mustQueries.isEmpty()) b.must(mustQueries);
            if (!filterQueries.isEmpty()) b.filter(filterQueries);
            if (mustQueries.isEmpty() && filterQueries.isEmpty()) {
                b.must(Query.of(q2 -> q2.matchAll(ma -> ma)));
            }
            return b;
        }));

        SortOrder sortOrder = "asc".equalsIgnoreCase(order) ? SortOrder.Asc : SortOrder.Desc;

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size))
                .withSort(s -> s.field(f -> f.field(sortBy).order(sortOrder)))
                .build();

        return elasticsearchOperations.search(nativeQuery, HotelDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 🏨 Search hotels by specific amenities
     */
    @Cacheable(value = "hotels", key = "'amenities-' + #amenities + '-' + #location + '-' + #page")
    public List<HotelDocument> searchByAmenities(List<String> amenities, String location,
                                                 int page, int size) {
        log.info("Searching hotels with amenities: {}", amenities);

        List<Query> mustQueries = new ArrayList<>();

        // Amenities filter - hotel must have ALL specified amenities
        List<FieldValue> amenityValues = amenities.stream()
                .map(FieldValue::of)
                .collect(Collectors.toList());

        mustQueries.add(Query.of(q -> q
                .terms(t -> t.field("amenities").terms(tv -> tv.value(amenityValues)))
        ));

        // Optional location filter
        if (location != null && !location.isEmpty()) {
            mustQueries.add(Query.of(q -> q
                    .bool(b -> b
                            .should(s1 -> s1.match(m -> m.field("city").query(location)))
                            .should(s2 -> s2.match(m -> m.field("country").query(location)))
                            .minimumShouldMatch("1")
                    )
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(b -> b.must(mustQueries)));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size))
                .withSort(s -> s.field(f -> f.field("rating").order(SortOrder.Desc)))
                .build();

        return elasticsearchOperations.search(nativeQuery, HotelDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 🔎 Autocomplete hotel names
     */
    @Cacheable(value = "hotels", key = "'autocomplete-' + #prefix")
    public List<HotelDocument> autocompleteHotelName(String prefix) {
        log.info("Autocomplete search for: {}", prefix);

        Query prefixQuery = Query.of(q -> q
                .multiMatch(m -> m
                        .query(prefix)
                        .fields("name^2", "city")
                        .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BoolPrefix)
                )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(prefixQuery)
                .withPageable(PageRequest.of(0, 10))
                .build();

        return elasticsearchOperations.search(nativeQuery, HotelDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 🛏️ Search available rooms by criteria
     */
    @Cacheable(value = "rooms", key = "'available-' + #city + '-' + #roomType + '-' + #maxPrice + '-' + #guests + '-' + #page")
    public List<RoomTypeDocument> searchAvailableRooms(String city, String roomType,
                                                       BigDecimal maxPrice, int guests,
                                                       int page, int size) {
        log.info("Searching available rooms - city: {}, type: {}, maxPrice: {}", city, roomType, maxPrice);

        List<Query> mustQueries = new ArrayList<>();
        List<Query> filterQueries = new ArrayList<>();

        // Room type filter
        if (roomType != null && !roomType.isEmpty()) {
            mustQueries.add(Query.of(q -> q
                    .match(m -> m.field("roomType").query(roomType).fuzziness("AUTO"))
            ));
        }

        // Price filter
        if (maxPrice != null) {
            filterQueries.add(Query.of(q -> q
                    .range(r -> r
                            .number(n -> n
                                    .field("pricePerNight")
                                    .lte(maxPrice.doubleValue())
                            )
                    )
            ));
        }

        // Availability filter based on guests
        int minRooms = (int) Math.ceil(guests / 2.0);
        filterQueries.add(Query.of(q -> q
                .range(r -> r.number(n -> n.field("totalRooms").gte((double) minRooms)))
        ));

        // City filter (using denormalized hotel name field)
        if (city != null && !city.isEmpty()) {
            mustQueries.add(Query.of(q -> q
                    .match(m -> m.field("hotelName").query(city))
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(b -> {
            if (!mustQueries.isEmpty()) b.must(mustQueries);
            if (!filterQueries.isEmpty()) b.filter(filterQueries);
            if (mustQueries.isEmpty() && filterQueries.isEmpty()) {
                b.must(Query.of(q2 -> q2.matchAll(ma -> ma)));
            }
            return b;
        }));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size))
                .withSort(s -> s.field(f -> f.field("pricePerNight").order(SortOrder.Asc)))
                .build();

        return elasticsearchOperations.search(nativeQuery, RoomTypeDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 🏷️ Search rooms by specific facilities
     */
    @Cacheable(value = "rooms", key = "'facilities-' + #facilities + '-' + #city + '-' + #page")
    public List<RoomTypeDocument> searchRoomsByFacilities(List<String> facilities, String city,
                                                          int page, int size) {
        log.info("Searching rooms with facilities: {}", facilities);

        List<Query> mustQueries = new ArrayList<>();

        // Facilities filter
        List<FieldValue> facilityValues = facilities.stream()
                .map(FieldValue::of)
                .collect(Collectors.toList());

        mustQueries.add(Query.of(q -> q
                .terms(t -> t.field("facilities").terms(tv -> tv.value(facilityValues)))
        ));

        // Optional city filter
        if (city != null && !city.isEmpty()) {
            mustQueries.add(Query.of(q -> q
                    .match(m -> m.field("hotelName").query(city))
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(b -> b.must(mustQueries)));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size))
                .build();

        return elasticsearchOperations.search(nativeQuery, RoomTypeDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 💎 Search budget-friendly hotels
     */
    @Cacheable(value = "hotels", key = "'budget-' + #maxPrice + '-' + #city + '-' + #page")
    public List<HotelDocument> searchBudgetHotels(BigDecimal maxPrice, String city,
                                                  int page, int size) {
        log.info("Searching budget hotels with max price: {}", maxPrice);

        List<Query> mustQueries = new ArrayList<>();

        mustQueries.add(Query.of(q -> q
                .range(r -> r
                        .number(n -> n.field("maxPrice").lte(maxPrice.doubleValue()))
                )
        ));

        if (city != null && !city.isEmpty()) {
            mustQueries.add(Query.of(q -> q
                    .term(t -> t.field("city.keyword").value(city))
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(b -> b.must(mustQueries)));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size))
                .withSort(s -> s.field(f -> f.field("minPrice").order(SortOrder.Asc)))
                .build();

        return elasticsearchOperations.search(nativeQuery, HotelDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 🌟 Search luxury hotels (high rating + premium amenities)
     */
    @Cacheable(value = "hotels", key = "'luxury-' + #city + '-' + #page")
    public List<HotelDocument> searchLuxuryHotels(String city, int page, int size) {
        log.info("Searching luxury hotels");

        List<Query> mustQueries = new ArrayList<>();

        // High rating filter (4+ stars)
        mustQueries.add(Query.of(q -> q
                .range(r -> r.number(n -> n.field("rating").gte(4.0)))
        ));

        // Premium price indicator
        mustQueries.add(Query.of(q -> q
                .range(r -> r.number(n -> n.field("minPrice").gte(200.0)))
        ));

        if (city != null && !city.isEmpty()) {
            mustQueries.add(Query.of(q -> q
                    .term(t -> t.field("city.keyword").value(city))
            ));
        }

        Query boolQuery = Query.of(q -> q.bool(b -> b.must(mustQueries)));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size))
                .withSort(s -> s.field(f -> f.field("rating").order(SortOrder.Desc)))
                .build();

        return elasticsearchOperations.search(nativeQuery, HotelDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 🔥 Get top-rated hotels in a location
     */
    @Cacheable(value = "hotels", key = "'top-rated-' + #city + '-' + #limit")
    public List<HotelDocument> getTopRatedHotels(String city, int limit) {
        log.info("Getting top-rated hotels in: {}", city);

        Query query;
        if (city != null && !city.isEmpty()) {
            query = Query.of(q -> q
                    .term(t -> t.field("city.keyword").value(city))
            );
        } else {
            query = Query.of(q -> q.matchAll(m -> m));
        }

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withPageable(PageRequest.of(0, limit))
                .withSort(s -> s.field(f -> f.field("rating").order(SortOrder.Desc)))
                .build();

        return elasticsearchOperations.search(nativeQuery, HotelDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 📊 Get hotel availability summary
     * Note: This is a simplified version. In production, you'd integrate with a booking system.
     */
    @Cacheable(value = "hotels", key = "'availability-' + #hotelId + '-' + #checkIn + '-' + #checkOut")
    public Map<String, Object> getHotelAvailability(String hotelId, LocalDate checkIn, LocalDate checkOut) {
        log.info("Getting availability for hotel: {} from {} to {}", hotelId, checkIn, checkOut);

        Query idQuery = Query.of(q -> q.term(t -> t.field("id").value(hotelId)));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(idQuery)
                .build();

        Optional<HotelDocument> hotelOpt = elasticsearchOperations.search(nativeQuery, HotelDocument.class)
                .map(SearchHit::getContent)
                .stream()
                .findFirst();

        Map<String, Object> result = new HashMap<>();

        if (hotelOpt.isPresent()) {
            HotelDocument hotel = hotelOpt.get();
            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

            result.put("hotelId", hotelId);
            result.put("hotelName", hotel.getName());
            result.put("checkIn", checkIn);
            result.put("checkOut", checkOut);
            result.put("nights", nights);
            result.put("totalRoomsAvailable", hotel.getTotalRoomsAvailable());
            result.put("roomTypes", hotel.getRoomTypes());
            result.put("minPricePerNight", hotel.getMinPrice());
            result.put("estimatedTotalCost", hotel.getMinPrice() * nights);
        }

        return result;
    }

    /**
     * 🗺️ Search hotels within a geographical bounding box
     */
    @Cacheable(value = "hotels", key = "'bounds-' + #neLat + '-' + #neLon + '-' + #swLat + '-' + #swLon + '-' + #page")
    public List<HotelDocument> searchHotelsInBounds(double neLat, double neLon, double swLat,
                                                    double swLon, int page, int size) {
        log.info("Searching hotels in bounds: NE({}, {}) SW({}, {})", neLat, neLon, swLat, swLon);

        Query geoBoundingBoxQuery = Query.of(q -> q
                .nested(n -> n
                        .path("address")
                        .query(nq -> nq
                                .geoBoundingBox(g -> g
                                        .field("address.location")
                                        .boundingBox(b -> b
                                                .tlbr(t -> t
                                                        .topLeft(tl -> tl.latlon(ll -> ll.lat(neLat).lon(swLon)))
                                                        .bottomRight(br -> br.latlon(ll -> ll.lat(swLat).lon(neLon)))
                                                )
                                        )
                                )
                        )
                )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(geoBoundingBoxQuery)
                .withPageable(PageRequest.of(page, size))
                .build();

        return elasticsearchOperations.search(nativeQuery, HotelDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 🔍 Full-text search across hotel name and description
     */
    @Cacheable(value = "hotels", key = "'text-' + #query + '-' + #page")
    public List<HotelDocument> fullTextSearch(String query, int page, int size) {
        log.info("Full-text search for: {}", query);

        Query multiMatchQuery = Query.of(q -> q
                .multiMatch(m -> m
                        .query(query)
                        .fields("name^3", "description^2", "city", "amenities")
                        .fuzziness("AUTO")
                        .type(co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType.BestFields)
                )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(multiMatchQuery)
                .withPageable(PageRequest.of(page, size))
                .build();

        return elasticsearchOperations.search(nativeQuery, HotelDocument.class)
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 📍 Search hotels along a route (multiple waypoints)
     */
    public List<HotelDocument> searchHotelsAlongRoute(List<Map<String, Double>> waypoints, String radius) {
        log.info("Searching hotels along route with {} waypoints", waypoints.size());

        List<HotelDocument> allHotels = new ArrayList<>();

        // Search near each waypoint
        for (Map<String, Double> waypoint : waypoints) {
            double lat = waypoint.get("lat");
            double lon = waypoint.get("lon");

            List<HotelDocument> hotelsNearWaypoint = searchHotelsNearby(lat, lon, radius, 1, 0, 10);
            allHotels.addAll(hotelsNearWaypoint);
        }

        // Remove duplicates based on hotel ID
        return allHotels.stream()
                .collect(Collectors.toMap(
                        HotelDocument::getId,
                        h -> h,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(HotelDocument::getRating).reversed())
                .collect(Collectors.toList());
    }

    // ==================== Cache Eviction Methods ====================

    @CacheEvict(value = "hotels", allEntries = true)
    public void evictHotelsCache() {
        log.info("Evicting all hotels cache");
    }

    @CacheEvict(value = "rooms", allEntries = true)
    public void evictRoomsCache() {
        log.info("Evicting all rooms cache");
    }

    @CacheEvict(value = {"hotels", "rooms"}, allEntries = true)
    public void evictAllCaches() {
        log.info("Evicting all hotel-related caches");
    }
}