package com.mmtext.supplieradapterservice.repo;

import com.mmtext.supplieradapterservice.model.RoomTypePolled;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoomTypePolledRepo extends JpaRepository<RoomTypePolled,Long> {
    // Find by supplier reference
    Optional<RoomTypePolled> findByRef(String ref);

    // Find all room types belonging to a hotel where updatedAt < given time
    List<RoomTypePolled> findByHotelRefAndUpdatedAtBefore(String hotelRef, Instant lastModified);

    List<RoomTypePolled> findByRoomTypeIn(Collection<String> roomTypes);
}
