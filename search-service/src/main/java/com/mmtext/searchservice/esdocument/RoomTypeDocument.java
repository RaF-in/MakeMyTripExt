package com.mmtext.searchservice.esdocument;


import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Elasticsearch document for RoomType data.
 * Represents room information as a nested object within Hotel documents
 * or as standalone documents for specialized queries.
 */

@Document(indexName = "room_types")
public class RoomTypeDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String roomType;

    @Field(type = FieldType.Integer)
    private Integer totalRooms;

    @Field(type = FieldType.Double)
    private BigDecimal pricePerNight;

    /**
     * Reference to the parent hotel ID for queries.
     */
    @Field(type = FieldType.Keyword)
    private String hotelId;

    @Field(type = FieldType.Keyword)
    private String ref;

    /**
     * Room facilities with keyword type for exact matching and aggregations.
     */
    @Field(type = FieldType.Keyword)
    private List<String> facilities;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    @Field(type = FieldType.Date)
    private Instant updatedAt;

    /**
     * Denormalized hotel name for easier searching without joins.
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String hotelName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(Integer totalRooms) {
        this.totalRooms = totalRooms;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities;
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

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }
}