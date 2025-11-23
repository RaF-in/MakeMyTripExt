package com.mmtext.searchservice.esdocument;


import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;

/**
 * Elasticsearch document for Hotel data.
 * Denormalized structure with nested address and room types for optimal search performance.
 */

@Document(indexName = "hotels")
public class HotelDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    /**
     * Nested address object for complex queries on location data.
     */
    @Field(type = FieldType.Nested, includeInParent = true)
    private AddressDocument address;

    @Field(type = FieldType.Double)
    private Double rating;

    /**
     * Hotel amenities with keyword type for exact matching and faceted search.
     */
    @Field(type = FieldType.Keyword)
    private List<String> amenities;

    /**
     * Nested room types for searching by room characteristics.
     */
    @Field(type = FieldType.Nested, includeInParent = true)
    private List<RoomTypeDocument> roomTypes;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    @Field(type = FieldType.Date)
    private Instant updatedAt;

    @Field(type = FieldType.Keyword)
    private String ref;

    @Field(type = FieldType.Keyword)
    private String supplierRef;

    /**
     * Computed fields for easier filtering and sorting.
     */
    @Field(type = FieldType.Integer)
    private Integer totalRoomsAvailable;

    @Field(type = FieldType.Double)
    private Double minPrice;

    @Field(type = FieldType.Double)
    private Double maxPrice;

    /**
     * Flattened location fields for quick access without nested queries.
     */
    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Keyword)
    private String country;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AddressDocument getAddress() {
        return address;
    }

    public void setAddress(AddressDocument address) {
        this.address = address;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public List<RoomTypeDocument> getRoomTypes() {
        return roomTypes;
    }

    public void setRoomTypes(List<RoomTypeDocument> roomTypes) {
        this.roomTypes = roomTypes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getSupplierRef() {
        return supplierRef;
    }

    public void setSupplierRef(String supplierRef) {
        this.supplierRef = supplierRef;
    }

    public Integer getTotalRoomsAvailable() {
        return totalRoomsAvailable;
    }

    public void setTotalRoomsAvailable(Integer totalRoomsAvailable) {
        this.totalRoomsAvailable = totalRoomsAvailable;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}