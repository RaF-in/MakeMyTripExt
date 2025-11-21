package com.mmtext.supplieradapterservice.service;

import com.mmtext.supplieradapterservice.dto.RoomInventoryItem;
import com.mmtext.supplieradapterservice.model.HotelPolled;
import com.mmtext.supplieradapterservice.model.RoomTypePolled;
import com.mmtext.supplieradapterservice.repo.HotelPolledRepo;
import com.mmtext.supplieradapterservice.repo.RoomTypePolledRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoomTypePollingService {

    private final RoomTypePolledRepo roomRepo;
    private final HotelPolledRepo hotelRepo;

    public RoomTypePollingService(RoomTypePolledRepo roomRepo, HotelPolledRepo hotelRepo) {
        this.roomRepo = roomRepo;
        this.hotelRepo = hotelRepo;
    }

    @Transactional
    public void syncRoomTypes(List<RoomInventoryItem> supplierRooms) {

        // Extract refs upfront
        Set<String> hotelRefs = supplierRooms.stream()
                .map(RoomInventoryItem::getSupplierRef)
                .collect(Collectors.toSet());

        // Load all existing hotels at once
        Map<String, HotelPolled> existingHotels =
                hotelRepo.findByRefIn(hotelRefs).stream()
                        .collect(Collectors.toMap(HotelPolled::getRef, h -> h));

        // Load all existing room types at once
        Set<String> roomTypes = supplierRooms.stream()
                .map(RoomInventoryItem::getRoomType)
                .collect(Collectors.toSet());

        Map<String, RoomTypePolled> existingRooms =
                roomRepo.findByRoomTypeIn(roomTypes).stream()
                        .collect(Collectors.toMap(RoomTypePolled::getRoomType, r -> r));

        List<HotelPolled> hotelsToSave = new ArrayList<>();
        List<RoomTypePolled> roomsToSave = new ArrayList<>();

        for (RoomInventoryItem dto : supplierRooms) {

            // 1. Find or create hotel
            HotelPolled hotel = existingHotels.get(dto.getSupplierRef());
            if (hotel == null) {
                hotel = createNewHotel(dto);
                existingHotels.put(dto.getSupplierRef(), hotel);
            }

            // 2. Find existing roomType
            RoomTypePolled room = existingRooms.get(dto.getRoomType());

            if (room == null) { // create new room
                room = mapNewRoom(dto, hotel);
                existingRooms.put(dto.getRoomType(), room);
                roomsToSave.add(room);

                hotel.setUpdatedAt(Instant.now());
                hotelsToSave.add(hotel);
                continue;
            }

            // 3. Update if older
            Instant lastModified = dto.getUpdatedAt();
            if (room.getUpdatedAt().isBefore(lastModified)) {

                updateExisting(room, dto);
                roomsToSave.add(room);

                hotel.setUpdatedAt(Instant.now());
                hotelsToSave.add(hotel);
            }
        }

        // BULK SAVE
        hotelRepo.saveAll(hotelsToSave);
        roomRepo.saveAll(roomsToSave);
    }


    private HotelPolled createNewHotel(RoomInventoryItem dto) {
        HotelPolled hotel = new HotelPolled();
        hotel.setRef(dto.getSupplierRef());
        hotel.setName(dto.getHotelName());
        hotel.setCreatedAt(Instant.now());
        hotel.setUpdatedAt(Instant.now());
        return hotelRepo.save(hotel);
    }

    private RoomTypePolled mapNewRoom(RoomInventoryItem dto, HotelPolled hotel) {
        RoomTypePolled rt = new RoomTypePolled();
        rt.setRoomType(dto.getRoomType());
        rt.setPricePerNight(dto.getPrice());
        rt.setTotalRooms(dto.getSeatsAvailable());
        rt.setRef(dto.supplierRef());
        rt.setHotel(hotel);
        rt.setCreatedAt(Instant.now());
        rt.setUpdatedAt(Instant.now());
        return rt;
    }

    private void updateExisting(RoomTypePolled existing, RoomInventoryItem dto) {
        existing.setRoomType(dto.getRoomType());
        existing.setTotalRooms(dto.getSeatsAvailable());
        existing.setPricePerNight(dto.getPrice());
        existing.setUpdatedAt(Instant.now());
    }
}

