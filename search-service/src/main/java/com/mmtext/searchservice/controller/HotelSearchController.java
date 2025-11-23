package com.mmtext.searchservice.controller;

import com.mmtext.searchservice.esdocument.HotelDocument;
import com.mmtext.searchservice.esdocument.RoomTypeDocument;
import com.mmtext.searchservice.service.HotelSearchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for hotel search operations.
 * Provides comprehensive search capabilities for OTA (Online Travel Agency) systems.
 */
@RestController
@RequestMapping("/api/hotel-search")
public class HotelSearchController {

    private final HotelSearchService hotelSearchService;

    public HotelSearchController(HotelSearchService hotelSearchService) {
        this.hotelSearchService = hotelSearchService;
    }

    /**
     * 🔍 Fuzzy search hotels by name
     * GET /api/hotel-search/by-name?name=Hilton&page=0&size=20
     *
     * Examples:
     * - "Hilton" will match "Hilton Hotel", "The Hilton", "Hilton Garden Inn"
     * - "Marriot" (typo) will match "Marriott"
     * - "Hyat" will match "Hyatt"
     */
    @GetMapping("/by-name")
    public ResponseEntity<List<HotelDocument>> searchByHotelName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<HotelDocument> hotels = hotelSearchService.searchByHotelName(name, page, size);
        return ResponseEntity.ok(hotels);
    }

    /**
     * 🔍 Search hotels by destination (city/country)
     * GET /api/hotel-search/destination?location=Dubai&checkIn=2025-12-01&checkOut=2025-12-05&guests=2
     */
    @GetMapping("/destination")
    public ResponseEntity<List<HotelDocument>> searchByDestination(
            @RequestParam String location,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") int guests,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<HotelDocument> hotels = hotelSearchService.searchByDestination(
                location, checkIn, checkOut, guests, page, size);
        return ResponseEntity.ok(hotels);
    }

    /**
     * 📍 Search hotels near a location (geo-search)
     * GET /api/hotel-search/nearby?lat=25.2048&lon=55.2708&radius=5km&guests=2
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<HotelDocument>> searchNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10km") String radius,
            @RequestParam(defaultValue = "1") int guests,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<HotelDocument> hotels = hotelSearchService.searchHotelsNearby(
                lat, lon, radius, guests, page, size);
        return ResponseEntity.ok(hotels);
    }

    /**
     * 💰 Filter hotels by price range
     * GET /api/hotel-search/price-range?minPrice=50&maxPrice=200&city=Dubai
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<HotelDocument>> searchByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<HotelDocument> hotels = hotelSearchService.searchByPriceRange(
                minPrice, maxPrice, city, page, size);
        return ResponseEntity.ok(hotels);
    }

    /**
     * ⭐ Filter hotels by rating
     * GET /api/hotel-search/rating?minRating=4.0&city=Dubai
     */
    @GetMapping("/rating")
    public ResponseEntity<List<HotelDocument>> searchByRating(
            @RequestParam Double minRating,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<HotelDocument> hotels = hotelSearchService.searchByRating(
                minRating, city, page, size);
        return ResponseEntity.ok(hotels);
    }

    /**
     * 🎯 Advanced hotel search with multiple filters
     * GET /api/hotel-search/advanced?location=Dubai&minPrice=100&maxPrice=500&minRating=4.0&amenities=WiFi,Pool&sortBy=price&order=asc
     */
    @GetMapping("/advanced")
    public ResponseEntity<List<HotelDocument>> advancedSearch(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) List<String> amenities,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<HotelDocument> hotels = hotelSearchService.advancedHotelSearch(
                location, minPrice, maxPrice, minRating, amenities, sortBy, order, page, size);
        return ResponseEntity.ok(hotels);
    }

    /**
     * 🏨 Search hotels by amenities
     * GET /api/hotel-search/amenities?amenities=WiFi,Pool,Gym&location=Dubai
     */
    @GetMapping("/amenities")
    public ResponseEntity<List<HotelDocument>> searchByAmenities(
            @RequestParam List<String> amenities,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<HotelDocument> hotels = hotelSearchService.searchByAmenities(
                amenities, location, page, size);
        return ResponseEntity.ok(hotels);
    }

    /**
     * 🔎 Autocomplete hotel names
     * GET /api/hotel-search/autocomplete?prefix=Hil
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<HotelDocument>> autocompleteHotelName(
            @RequestParam String prefix) {
        List<HotelDocument> hotels = hotelSearchService.autocompleteHotelName(prefix);
        return ResponseEntity.ok(hotels);
    }

    /**
     * 🛏️ Search available rooms by criteria
     * GET /api/hotel-search/rooms/available?city=Dubai&roomType=Deluxe&maxPrice=300&guests=2
     */
    @GetMapping("/rooms/available")
    public ResponseEntity<List<RoomTypeDocument>> searchAvailableRooms(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "1") int guests,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<RoomTypeDocument> rooms = hotelSearchService.searchAvailableRooms(
                city, roomType, maxPrice, guests, page, size);
        return ResponseEntity.ok(rooms);
    }

    /**
     * 🏷️ Search rooms by facilities
     * GET /api/hotel-search/rooms/facilities?facilities=Balcony,Sea View&city=Dubai
     */
    @GetMapping("/rooms/facilities")
    public ResponseEntity<List<RoomTypeDocument>> searchRoomsByFacilities(
            @RequestParam List<String> facilities,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<RoomTypeDocument> rooms = hotelSearchService.searchRoomsByFacilities(
                facilities, city, page, size);
        return ResponseEntity.ok(rooms);
    }

    /**
     * 💎 Get budget-friendly hotels
     * GET /api/hotel-search/budget?maxPrice=150&city=Dubai
     */
    @GetMapping("/budget")
    public ResponseEntity<List<HotelDocument>> searchBudgetHotels(
            @RequestParam BigDecimal maxPrice,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<HotelDocument> hotels = hotelSearchService.searchBudgetHotels(
                maxPrice, city, page, size);
        return ResponseEntity.ok(hotels);
    }

    /**
     * 🌟 Get luxury hotels (high rating + premium price)
     * GET /api/hotel-search/luxury?city=Dubai
     */
    @GetMapping("/luxury")
    public ResponseEntity<List<HotelDocument>> searchLuxuryHotels(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<HotelDocument> hotels = hotelSearchService.searchLuxuryHotels(
                city, page, size);
        return ResponseEntity.ok(hotels);
    }

    /**
     * 🔥 Get top-rated hotels in a location
     * GET /api/hotel-search/top-rated?city=Dubai&limit=10
     */
    @GetMapping("/top-rated")
    public ResponseEntity<List<HotelDocument>> getTopRatedHotels(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "10") int limit) {
        List<HotelDocument> hotels = hotelSearchService.getTopRatedHotels(city, limit);
        return ResponseEntity.ok(hotels);
    }

    /**
     * 📊 Get hotel availability summary
     * GET /api/hotel-search/availability?hotelId=12345&checkIn=2025-12-01&checkOut=2025-12-05
     */
    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> getHotelAvailability(
            @RequestParam String hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        Map<String, Object> availability = hotelSearchService.getHotelAvailability(
                hotelId, checkIn, checkOut);
        return ResponseEntity.ok(availability);
    }

    /**
     * 🗺️ Search hotels in a geographical bounding box
     * GET /api/hotel-search/bounds?neLat=25.3&neLon=55.4&swLat=25.1&swLon=55.2
     */
    @GetMapping("/bounds")
    public ResponseEntity<List<HotelDocument>> searchInBounds(
            @RequestParam double neLat,
            @RequestParam double neLon,
            @RequestParam double swLat,
            @RequestParam double swLon,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<HotelDocument> hotels = hotelSearchService.searchHotelsInBounds(
                neLat, neLon, swLat, swLon, page, size);
        return ResponseEntity.ok(hotels);
    }

    /**
     * 🔍 Full-text search across hotel name and description
     * GET /api/hotel-search/text?query=beach resort spa
     */
    @GetMapping("/text")
    public ResponseEntity<List<HotelDocument>> fullTextSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<HotelDocument> hotels = hotelSearchService.fullTextSearch(query, page, size);
        return ResponseEntity.ok(hotels);
    }

    /**
     * 📍 Search hotels along a route (multiple waypoints)
     * POST /api/hotel-search/route
     */
    @PostMapping("/route")
    public ResponseEntity<List<HotelDocument>> searchAlongRoute(
            @RequestBody List<Map<String, Double>> waypoints,
            @RequestParam(defaultValue = "5km") String radius) {
        List<HotelDocument> hotels = hotelSearchService.searchHotelsAlongRoute(waypoints, radius);
        return ResponseEntity.ok(hotels);
    }

    // ==================== Cache Management Endpoints ====================

    /**
     * 🗑️ Clear hotels cache
     * DELETE /api/hotel-search/cache/hotels
     */
    @DeleteMapping("/cache/hotels")
    public ResponseEntity<Map<String, String>> clearHotelsCache() {
        hotelSearchService.evictHotelsCache();
        return ResponseEntity.ok(Map.of("message", "Hotels cache cleared successfully"));
    }

    /**
     * 🗑️ Clear rooms cache
     * DELETE /api/hotel-search/cache/rooms
     */
    @DeleteMapping("/cache/rooms")
    public ResponseEntity<Map<String, String>> clearRoomsCache() {
        hotelSearchService.evictRoomsCache();
        return ResponseEntity.ok(Map.of("message", "Rooms cache cleared successfully"));
    }

    /**
     * 🗑️ Clear all hotel-related caches
     * DELETE /api/hotel-search/cache/all
     */
    @DeleteMapping("/cache/all")
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        hotelSearchService.evictAllCaches();
        return ResponseEntity.ok(Map.of("message", "All hotel caches cleared successfully"));
    }
}