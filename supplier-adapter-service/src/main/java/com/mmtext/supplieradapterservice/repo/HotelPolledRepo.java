package com.mmtext.supplieradapterservice.repo;

import com.mmtext.supplieradapterservice.model.HotelPolled;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HotelPolledRepo extends JpaRepository<HotelPolled, Long> {
    Optional<HotelPolled> findByRef(String ref);
    List<HotelPolled> findByRefIn(Collection<String> refs);
}
