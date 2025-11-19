package com.mmtext.listingservice.mapper;

import com.mmtext.listingservice.dto.HotelResponseDTO;
import com.mmtext.listingservice.model.Hotel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HotelMapper {
    public static List<HotelResponseDTO> toDTOList(List<Hotel> hotels) {
        List<HotelResponseDTO> dtos = new ArrayList<>();
        hotels.forEach(hotel -> {
            HotelResponseDTO dto = new HotelResponseDTO();
            dto.setHotel(hotel);
            dtos.add(dto);
        });
        return dtos;
    }

    public static HotelResponseDTO toDTO(Hotel hotel) {
        HotelResponseDTO dto = new HotelResponseDTO();
        dto.setHotel(hotel);
        return dto;
    }
}
