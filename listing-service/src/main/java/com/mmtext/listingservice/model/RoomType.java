package com.mmtext.listingservice.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomType;
    private int totalRooms;
    private double pricePerNight;
    @ManyToOne(fetch = FetchType.LAZY)
    private Hotel hotel;

    @ElementCollection
    private List<String> facilities;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }
}

