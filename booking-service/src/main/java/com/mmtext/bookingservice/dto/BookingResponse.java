package com.mmtext.bookingservice.dto;

import com.mmtext.bookingservice.enums.Enums;

import java.time.Instant;

public class BookingResponse {
    private String bookingReference;
    private String ticketId;
    private Enums.BookingStatus status;
    private String paymentUrl;
    private Instant expiredAt;
    private String message;
    private Integer queuePosition;

    public BookingResponse() {}

    public BookingResponse(String bookingReference, String ticketId, Enums.BookingStatus status,
                           String paymentUrl, Instant expiredAt, String message) {
        this.bookingReference = bookingReference;
        this.ticketId = ticketId;
        this.status = status;
        this.paymentUrl = paymentUrl;
        this.expiredAt = expiredAt;
        this.message = message;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public Enums.BookingStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.BookingStatus status) {
        this.status = status;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
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

    public Integer getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }
}
