package com.mmtext.listingservice.dto;

import com.mmtext.listingservice.model.Hotel;
import com.mmtext.listingservice.model.RoomType;

import java.util.List;

public class HotelResponseDTO {
    private Hotel hotel;
    private List<RoomType> roomTypes;

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public List<RoomType> getRoomTypes() {
        return roomTypes;
    }

    public void setRoomTypes(List<RoomType> roomTypes) {
        this.roomTypes = roomTypes;
    }
}
