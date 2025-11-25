package com.mmtext.bookingservice.repo;

import com.mmtext.bookingservice.enums.Enums;
import com.mmtext.bookingservice.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByUserId(String userId);

    Optional<Booking> findByBookingReferenceAndUserId(String bookingReference, String userId);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.expiredAt < :now")
    List<Booking> findExpiredBookings(@Param("status") Enums.BookingStatus status,
                                      @Param("now") Instant now);

    @Modifying
    @Query("UPDATE Booking b SET b.status = :newStatus, b.updatedAt = :now " +
            "WHERE b.status = :oldStatus AND b.expiredAt < :now")
    int expireOldBookings(@Param("oldStatus") Enums.BookingStatus oldStatus,
                          @Param("newStatus") Enums.BookingStatus newStatus,
                          @Param("now") Instant now);

    boolean existsByTicketIdAndStatusIn(String ticketId, List<Enums.BookingStatus> statuses);
}