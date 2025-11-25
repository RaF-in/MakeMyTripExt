package com.mmtext.bookingservice.enums;

public final class Enums {
    public enum ConcurrencyType {
        LOW,      // Hotel, Airline, Bus
        MEDIUM,   // Movie tickets
        HIGH      // Concert tickets
    }

    public enum BookingStatus {
        QUEUED,           // In virtual queue (HIGH concurrency only)
        PAYMENT_PENDING,  // Waiting for payment (stored in Redis, not DB)
        CONFIRMED,        // Payment successful, ticket booked in DB
        CANCELLED         // Booking cancelled
    }

    public enum TicketStatus {
        AVAILABLE,
        BOOKED
    }
}
