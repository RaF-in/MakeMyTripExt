package com.mmtext.bookingservice.dto;

import com.mmtext.bookingservice.enums.Enums;

/**
 * DTO for available ticket information
 */
public class AvailableTicketDTO {
    private String ticketId;
    private String eventId;
    private String eventName;
    private String seatNumber;
    private Double price;
    private Enums.ConcurrencyType concurrencyType;

    public AvailableTicketDTO() {}

    public AvailableTicketDTO(String ticketId, String eventId, String eventName,
                              String seatNumber, Double price, Enums.ConcurrencyType concurrencyType) {
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.seatNumber = seatNumber;
        this.price = price;
        this.concurrencyType = concurrencyType;
    }

    // Getters and Setters
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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Enums.ConcurrencyType getConcurrencyType() {
        return concurrencyType;
    }

    public void setConcurrencyType(Enums.ConcurrencyType concurrencyType) {
        this.concurrencyType = concurrencyType;
    }
}
