package com.mmtext.listingservice.controller;

import com.mmtext.listingservice.dto.HotelResponseDTO;
import com.mmtext.listingservice.mapper.HotelMapper;
import com.mmtext.listingservice.model.Hotel;
import com.mmtext.listingservice.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/hotel")
public class HotelController {
    @Autowired
    HotelService hotelService;
    @GetMapping
    public ResponseEntity<List<HotelResponseDTO>> getAllHotels() {
        return ResponseEntity.ok().body(HotelMapper.toDTOList(hotelService.getAllHotels()));
    }
    @PostMapping
    public ResponseEntity<HotelResponseDTO> createHotel(@RequestBody Hotel hotel) {
        return ResponseEntity.ok().body(hotelService.save(hotel));
    }
    @PutMapping
    public ResponseEntity<HotelResponseDTO> updateHotel(@RequestBody Hotel hotel) {
        return ResponseEntity.ok().body(hotelService.save(hotel));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteHotel(@PathVariable Long id) {
        return ResponseEntity.ok().body(hotelService.delete(id));
    }
}
