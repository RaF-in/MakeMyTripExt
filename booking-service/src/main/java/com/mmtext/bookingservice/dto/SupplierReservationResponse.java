package com.mmtext.bookingservice.dto;

import java.time.Instant;

public class SupplierReservationResponse {
    private boolean available;
    private String reservationId;
    private Instant expiredAt;
    private String message;

    public SupplierReservationResponse() {}

    public SupplierReservationResponse(boolean available, String reservationId,
                                       Instant expiredAt, String message) {
        this.available = available;
        this.reservationId = reservationId;
        this.expiredAt = expiredAt;
        this.message = message;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
