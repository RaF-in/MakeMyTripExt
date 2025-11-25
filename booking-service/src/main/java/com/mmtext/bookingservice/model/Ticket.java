package com.mmtext.bookingservice.model;

import com.mmtext.bookingservice.enums.Enums;
import jakarta.persistence.*;

import java.time.Instant;


@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_id", columnList = "ticketId"),
        @Index(name = "idx_event_id", columnList = "eventId"),
        @Index(name = "idx_status", columnList = "status")
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketId;

    @Column(nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String eventName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.ConcurrencyType concurrencyType;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.TicketStatus status;

    private String bookedByUserId;

    private String bookingReference;

    private Instant bookedAt;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (status == null) {
            status = Enums.TicketStatus.AVAILABLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Constructors
    public Ticket() {}

    public Ticket(String ticketId, String eventId, String eventName,
                  Enums.ConcurrencyType concurrencyType, Double price, String seatNumber) {
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.concurrencyType = concurrencyType;
        this.price = price;
        this.seatNumber = seatNumber;
        this.status = Enums.TicketStatus.AVAILABLE;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Enums.ConcurrencyType getConcurrencyType() {
        return concurrencyType;
    }

    public void setConcurrencyType(Enums.ConcurrencyType concurrencyType) {
        this.concurrencyType = concurrencyType;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Enums.TicketStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.TicketStatus status) {
        this.status = status;
    }

    public String getBookedByUserId() {
        return bookedByUserId;
    }

    public void setBookedByUserId(String bookedByUserId) {
        this.bookedByUserId = bookedByUserId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public Instant getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(Instant bookedAt) {
        this.bookedAt = bookedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}