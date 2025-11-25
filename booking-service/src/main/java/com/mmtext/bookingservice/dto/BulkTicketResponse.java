package com.mmtext.bookingservice.dto;

import java.util.List;

/**
 * Bulk ticket response
 */
public class BulkTicketResponse {
    private String eventId;
    private Integer totalCreated;
    private List<TicketResponse> tickets;

    public BulkTicketResponse() {}

    public BulkTicketResponse(String eventId, Integer totalCreated, List<TicketResponse> tickets) {
        this.eventId = eventId;
        this.totalCreated = totalCreated;
        this.tickets = tickets;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Integer getTotalCreated() {
        return totalCreated;
    }

    public void setTotalCreated(Integer totalCreated) {
        this.totalCreated = totalCreated;
    }

    public List<TicketResponse> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketResponse> tickets) {
        this.tickets = tickets;
    }
}
