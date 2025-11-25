package com.mmtext.bookingservice.dto;

import java.time.Instant;

/**
 * Queue status update DTO
 */
public class QueueStatusUpdate {
    private String bookingReference;
    private String status;
    private Integer queuePosition;
    private Integer totalInQueue;
    private Integer estimatedWaitSeconds;
    private String paymentUrl;
    private Instant expiredAt;

    public QueueStatusUpdate() {}

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(Integer queuePosition) {
        this.queuePosition = queuePosition;
    }

    public Integer getTotalInQueue() {
        return totalInQueue;
    }

    public void setTotalInQueue(Integer totalInQueue) {
        this.totalInQueue = totalInQueue;
    }

    public Integer getEstimatedWaitSeconds() {
        return estimatedWaitSeconds;
    }

    public void setEstimatedWaitSeconds(Integer estimatedWaitSeconds) {
        this.estimatedWaitSeconds = estimatedWaitSeconds;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Instant expiredAt) {
        this.expiredAt = expiredAt;
    }
}