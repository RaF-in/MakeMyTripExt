package com.mmtext.bookingservice.dto;

public class PaymentRequest {
    private String bookingReference;
    private Double amount;
    private String userId;

    public PaymentRequest() {}

    public PaymentRequest(String bookingReference, Double amount, String userId) {
        this.bookingReference = bookingReference;
        this.amount = amount;
        this.userId = userId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
