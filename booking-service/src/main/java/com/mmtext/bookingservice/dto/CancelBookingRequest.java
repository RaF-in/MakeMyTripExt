package com.mmtext.bookingservice.dto;

import com.mmtext.bookingservice.enums.Enums;

public class CancelBookingRequest {
    private String bookingReference;
    private Enums.BookingStatus status;
    private String message;
    private String refundId;
    private String userId;

    public void CancelBookingResponse() {}

    public void CancelBookingResponse(String bookingReference, Enums.BookingStatus status, String message) {
        this.bookingReference = bookingReference;
        this.status = status;
        this.message = message;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public Enums.BookingStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.BookingStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
