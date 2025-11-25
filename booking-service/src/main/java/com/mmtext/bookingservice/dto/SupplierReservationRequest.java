package com.mmtext.bookingservice.dto;

public class SupplierReservationRequest {
    private String ticketId;
    private String userId;

    public SupplierReservationRequest() {}

    public SupplierReservationRequest(String ticketId, String userId) {
        this.ticketId = ticketId;
        this.userId = userId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
