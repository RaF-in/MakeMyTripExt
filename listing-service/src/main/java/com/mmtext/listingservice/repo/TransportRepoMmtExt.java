package com.mmtext.listingservice.repo;


import com.mmtext.listingservice.model.AirCraft;
import com.mmtext.listingservice.model.Bus;
import com.mmtext.listingservice.model.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TransportRepoMmtExt extends JpaRepository<Transport,Long> {
    @Query("Select a From AirCraft a")
    List<AirCraft> getAllAirCrafts();
    @Query("Select a From Bus a")
    List<Bus> getAllAirBuses();
    @Query("SELECT a FROM AirCraft a WHERE a.code = :code")
    AirCraft findByAircraftCode(@Param("code") String code);
    @Query("SELECT a FROM Bus a WHERE a.busNum = :busNum")
    Bus findByBusNum(@Param("busNum") String busNum);
    @Query("SELECT COUNT(a) > 0 FROM AirCraft a WHERE a.code = :code AND a.id <> :id")
    boolean existsByAircraftCodeAndNotById(@Param("code") String code, @Param("id") Long id);
    @Query("SELECT COUNT(a) > 0 FROM Bus a WHERE a.busNum = :busNum AND a.id <> :id")
    boolean existsByBusNumAndNotById(@Param("busNum") String busNum, @Param("id") Long id);
    List<Transport> findByUpdatedAtGreaterThan(Instant lastModified);
}
