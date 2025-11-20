package com.mmtext.listingservice.service;

import com.mmtext.listingservice.model.RoomType;
import com.mmtext.listingservice.repo.RoomTypeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RoomTypeService {
    @Autowired
    private RoomTypeRepo roomTypeRepo;
    public List<RoomType> getAllRooms() {
        return roomTypeRepo.findAll();
    }
    public List<RoomType> findByUpdatedAtGreaterThan(Instant updatedAt) {
        return roomTypeRepo.findByUpdatedAtGreaterThan(updatedAt);
    }
}
