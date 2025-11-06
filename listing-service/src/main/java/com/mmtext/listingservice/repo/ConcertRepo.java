package com.mmtext.listingservice.repo;

import com.mmtext.listingservice.model.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertRepo extends JpaRepository<Concert,Long> {
}
