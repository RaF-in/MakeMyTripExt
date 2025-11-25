package com.mmtext.bookingservice.model;

import com.mmtext.bookingservice.enums.Enums;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_reference", columnList = "bookingReference"),
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_ticket_id", columnList = "ticketId")
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bookingReference;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String ticketId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.ConcurrencyType concurrencyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.BookingStatus status;

    private String supplierReservationId;

    @Column(nullable = false)
    private Double amount;


    private String paymentId;

    private Instant paidAt;

    private Instant cancelledAt;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // Constructors
    public Booking() {}

    public Booking(String bookingReference, String userId, String ticketId,
                   Enums.ConcurrencyType concurrencyType,  Double amount) {
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.ticketId = ticketId;
        this.concurrencyType = concurrencyType;
        this.status = status;
        this.amount = amount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
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

    public Enums.BookingStatus getStatus() {
        return status;
    }

    public void setStatus(Enums.BookingStatus status) {
        this.status = status;
    }

    public String getSupplierReservationId() {
        return supplierReservationId;
    }

    public void setSupplierReservationId(String supplierReservationId) {
        this.supplierReservationId = supplierReservationId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
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

