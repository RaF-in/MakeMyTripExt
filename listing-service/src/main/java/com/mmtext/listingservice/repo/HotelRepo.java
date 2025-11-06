package com.mmtext.listingservice.repo;

import com.mmtext.listingservice.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HotelRepo extends JpaRepository<Hotel, Long> {
}
