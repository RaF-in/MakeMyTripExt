package com.mmtext.supplieradapterservice.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;

@Entity
public class HotelPolled {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @OneToOne(cascade = CascadeType.ALL)
    private AddressPolled addresses;
    private double rating;
    @ElementCollection
    private List<String> amenities;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "hotel")
    private List<RoomTypePolled> roomTypes;
    private Instant createdAt;
    private Instant updatedAt;
    private String ref;
    private String supplierRef;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public AddressPolled getAddresses() {
        return addresses;
    }

    public void setAddresses(AddressPolled addresses) {
        this.addresses = addresses;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public List<RoomTypePolled> getRoomTypes() {
        return roomTypes;
    }

    public void setRoomTypes(List<RoomTypePolled> roomTypes) {
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
}
