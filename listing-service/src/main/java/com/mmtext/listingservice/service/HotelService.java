package com.mmtext.listingservice.service;

import com.mmtext.listingservice.dto.HotelResponseDTO;
import com.mmtext.listingservice.mapper.HotelMapper;
import com.mmtext.listingservice.model.Hotel;
import com.mmtext.listingservice.repo.HotelRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HotelService {
    @Autowired
    HotelRepo hotelRepo;
    public List<Hotel> getAllHotels() {
        return hotelRepo.findAll();
    }

    public HotelResponseDTO save(Hotel hotel) {
        hotelRepo.save(hotel);
        return HotelMapper.toDTO(hotel);
    }

    public Boolean delete(Long id) {
        hotelRepo.deleteById(id);
        return !hotelRepo.existsById(id);
    }
}
