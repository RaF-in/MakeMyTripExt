package com.mmtext.listingservice.controller;

import com.mmtext.listingservice.dto.HotelResponseDTO;
import com.mmtext.listingservice.mapper.HotelMapper;
import com.mmtext.listingservice.model.Hotel;
import com.mmtext.listingservice.model.RoomType;
import com.mmtext.listingservice.repo.RoomTypeRepo;
import com.mmtext.listingservice.service.HotelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/hotel")
public class HotelController {
    private static final Logger log = LoggerFactory.getLogger(HotelController.class);
    @Autowired
    HotelService hotelService;
    @Autowired
    private RoomTypeRepo roomTypeRepo;

    @GetMapping
    public ResponseEntity<List<HotelResponseDTO>> getAllHotels(
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch,
            @RequestHeader(value = HttpHeaders.IF_MODIFIED_SINCE, required = false) String ifModifiedSince
    ) {

        Instant clientLastModified = null;

        // Convert If-Modified-Since header to Instant
        if (ifModifiedSince != null) {
            try {
                clientLastModified = Instant.from(
                        DateTimeFormatter.RFC_1123_DATE_TIME.parse(ifModifiedSince)
                );
            } catch (Exception ignored) {}
        }

        List<Hotel> hotels;

        // 🔥 If client sent If-Modified-Since ⇒ filter by updatedAt > client value
        log.info("polling after time {}", clientLastModified);
        if (clientLastModified != null) {
            //List<RoomType> roomTypes = roomTypeRepo.findByUpdatedAtGreaterThan(clientLastModified);
            hotels = hotelService.getHotelsUpdatedAfter(clientLastModified);
            Instant lastModified = hotels.stream()
                    .map(Hotel::getUpdatedAt)       // you must have updatedAt on Hotel entity
                    .filter(Objects::nonNull)
                    .max(Instant::compareTo)
                    .orElse(Instant.now());         // fallback if no timestamps
            return ResponseEntity.ok()
                    .lastModified(lastModified.toEpochMilli())
                    .body(
                            HotelMapper.toDTOList(hotels)
                    );
        }
        hotels = hotelService.getAllHotels();
        return ResponseEntity.ok().body(HotelMapper.toDTOList(hotels));
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
