package com.mmtext.listingservice.dto;

import com.mmtext.listingservice.model.RoomType;

public class RoomTypeResponseDTO {
    private RoomType roomType;
    private Long  hotelId;
    private String hotelRef;
    private String hotelName;

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelRef() {
        return hotelRef;
    }

    public void setHotelRef(String hotelRef) {
        this.hotelRef = hotelRef;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }
}
