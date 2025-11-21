package com.mmtext.listingservice.mapper;

import com.mmtext.listingservice.dto.RoomTypeResponseDTO;
import com.mmtext.listingservice.model.RoomType;

import java.util.ArrayList;
import java.util.List;

public class RoomMapper {
    public static List<RoomTypeResponseDTO> toDTO(List<RoomType> roomTypes) {
        List<RoomTypeResponseDTO> response = new ArrayList<>();
        roomTypes.forEach(roomType -> {
            RoomTypeResponseDTO responseDTO = new RoomTypeResponseDTO();
            responseDTO.setRoomType(roomType);
            responseDTO.setHotelId(roomType.getHotel().getId());
            responseDTO.setHotelRef(roomType.getHotel().getRef());
            responseDTO.setHotelName(roomType.getHotel().getName());
            response.add(responseDTO);
        });
        return response;
    }
}
