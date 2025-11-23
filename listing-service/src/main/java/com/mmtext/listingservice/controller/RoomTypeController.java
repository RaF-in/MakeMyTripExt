package com.mmtext.listingservice.controller;
import com.mmtext.listingservice.dto.RoomTypeResponseDTO;
import com.mmtext.listingservice.mapper.RoomMapper;
import com.mmtext.listingservice.model.RoomType;
import com.mmtext.listingservice.service.RoomTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/roomType")
public class RoomTypeController {
    private static final Logger log = LoggerFactory.getLogger(RoomTypeController.class);

    @Autowired
    private RoomTypeService roomTypeService;

    @GetMapping
    public ResponseEntity<List<RoomTypeResponseDTO>> getAllRoomTypes(
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

        List<RoomType> roomTypes;

        // 🔥 If client sent If-Modified-Since ⇒ filter by updatedAt > client value
        log.info("polling after time {}", clientLastModified);
        if (clientLastModified != null) {
            roomTypes = roomTypeService.findByUpdatedAtGreaterThan(clientLastModified);

            Instant lastModified = roomTypes.stream()
                    .map(RoomType::getUpdatedAt)       // you must have updatedAt on Hotel entity
                    .filter(Objects::nonNull)
                    .max(Instant::compareTo)
                    .orElse(Instant.now());         // fallback if no timestamps
            return ResponseEntity.ok()
                    .lastModified(lastModified.toEpochMilli())
                    .body(
                            RoomMapper.toDTO(roomTypes)
                    );
        }
        roomTypes = roomTypeService.getAllRooms();
        return ResponseEntity.ok().body(RoomMapper.toDTO(roomTypes));
    }
}
