package com.mmtext.bookingservice.dto;

import com.mmtext.bookingservice.enums.Enums;

public class BookingRequest {
    private String userId;
    private String ticketId;
    private Enums.ConcurrencyType concurrencyType;
    private Double amount;

    public BookingRequest() {}

    public BookingRequest(String userId, String ticketId, Enums.ConcurrencyType concurrencyType, Double amount) {
        this.userId = userId;
        this.ticketId = ticketId;
        this.concurrencyType = concurrencyType;
        this.amount = amount;
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
}
