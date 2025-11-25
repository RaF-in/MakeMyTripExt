package com.mmtext.bookingservice.dto;

import com.mmtext.bookingservice.enums.Enums;

/**
 * Bulk add tickets request
 */
public class BulkAddTicketRequest {
    private String eventId;
    private String eventName;
    private Integer quantity;
    private String seatPrefix;
    private Double price;
    private Enums.ConcurrencyType concurrencyType;

    public BulkAddTicketRequest() {}

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSeatPrefix() {
        return seatPrefix;
    }

    public void setSeatPrefix(String seatPrefix) {
        this.seatPrefix = seatPrefix;
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
