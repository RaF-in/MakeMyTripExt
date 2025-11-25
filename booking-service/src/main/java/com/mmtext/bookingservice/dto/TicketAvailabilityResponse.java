package com.mmtext.bookingservice.dto;

/**
 * Ticket availability response DTO
 */
public class TicketAvailabilityResponse {
    private String ticketId;
    private boolean available;
    private String message;

    public TicketAvailabilityResponse() {}

    public TicketAvailabilityResponse(String ticketId, boolean available, String message) {
        this.ticketId = ticketId;
        this.available = available;
        this.message = message;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
