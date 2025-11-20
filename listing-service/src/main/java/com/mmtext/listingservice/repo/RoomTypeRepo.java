package com.mmtext.listingservice.repo;

import com.mmtext.listingservice.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface RoomTypeRepo extends JpaRepository<RoomType, Long> {
    List<RoomType> findByUpdatedAtGreaterThan(Instant lastModified);
}
