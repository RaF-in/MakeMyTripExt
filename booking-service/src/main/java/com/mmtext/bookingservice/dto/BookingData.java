package com.mmtext.bookingservice.dto;

import com.mmtext.bookingservice.enums.Enums;

import java.time.Instant;

/**
 * Booking data stored in Redis during PAYMENT_PENDING state
 */
public class BookingData {
    private String bookingReference;
    private String userId;
    private String ticketId;
    private String eventId;
    private Enums.ConcurrencyType concurrencyType;
    private Double amount;
    private String supplierReservationId;
    private Instant expiredAt;
    private String paymentUrl;

    public BookingData() {}

    public BookingData(String bookingReference, String userId, String ticketId,
                       String eventId, Enums.ConcurrencyType concurrencyType, Double amount,
                       Instant expiredAt) {
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.concurrencyType = concurrencyType;
        this.amount = amount;
        this.expiredAt = expiredAt;
    }

    // Getters and Setters
    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Enums.ConcurrencyType getConcurrencyType() {
        return concurrencyType;
    }

    public void setConcurrencyType(Enums.ConcurrencyType concurrencyType) {
        this.concurrencyType = concurrencyType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getSupplierReservationId() {
        return supplierReservationId;
    }

    public void setSupplierReservationId(String supplierReservationId) {
        this.supplierReservationId = supplierReservationId;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
}