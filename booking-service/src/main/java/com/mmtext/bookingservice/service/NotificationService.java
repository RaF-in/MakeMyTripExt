package com.mmtext.bookingservice.service;

import com.mmtext.bookingservice.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Send booking confirmation notification
     */
    @Async("asyncNotificationExecutor")
    public CompletableFuture<Boolean> sendBookingConfirmation(NotificationRequest request) {
        logger.info("Sending booking confirmation to user: {}, booking: {}",
                request.getUserId(), request.getBookingReference());

        // Simulate API call delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate email sending
        String emailBody = buildConfirmationEmail(request);
        logger.info("📧 Email sent to user {}: {}", request.getUserId(), emailBody);

        // Simulate SMS sending
        String smsBody = buildConfirmationSms(request);
        logger.info("📱 SMS sent to user {}: {}", request.getUserId(), smsBody);

        return CompletableFuture.completedFuture(true);
    }

    /**
     * Send booking failure notification
     */
    @Async("asyncNotificationExecutor")
    public CompletableFuture<Boolean> sendBookingFailure(NotificationRequest request) {
        logger.info("Sending booking failure notification to user: {}, booking: {}",
                request.getUserId(), request.getBookingReference());

        // Simulate API call delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate email sending
        String emailBody = buildFailureEmail(request);
        logger.info("📧 Email sent to user {}: {}", request.getUserId(), emailBody);

        // Simulate SMS sending
        String smsBody = buildFailureSms(request);
        logger.info("📱 SMS sent to user {}: {}", request.getUserId(), smsBody);

        return CompletableFuture.completedFuture(true);
    }

    /**
     * Send cancellation notification
     */
    @Async("asyncNotificationExecutor")
    public CompletableFuture<Boolean> sendCancellationNotification(NotificationRequest request) {
        logger.info("Sending cancellation notification to user: {}, booking: {}",
                request.getUserId(), request.getBookingReference());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String emailBody = buildCancellationEmail(request);
        logger.info("📧 Email sent to user {}: {}", request.getUserId(), emailBody);

        String smsBody = buildCancellationSms(request);
        logger.info("📱 SMS sent to user {}: {}", request.getUserId(), smsBody);

        return CompletableFuture.completedFuture(true);
    }

    /**
     * Send queue position update notification
     */
    @Async("asyncNotificationExecutor")
    public CompletableFuture<Boolean> sendQueuePositionUpdate(NotificationRequest request) {
        logger.info("Sending queue position update to user: {}, position: {}",
                request.getUserId(), request.getQueuePosition());

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String smsBody = String.format(
                "Your queue position: %d. Estimated wait: %d minutes. Booking ref: %s",
                request.getQueuePosition(),
                request.getEstimatedWaitMinutes(),
                request.getBookingReference()
        );

        logger.info("📱 SMS sent to user {}: {}", request.getUserId(), smsBody);

        return CompletableFuture.completedFuture(true);
    }

    /**
     * Send payment link notification
     */
    @Async("asyncNotificationExecutor")
    public CompletableFuture<Boolean> sendPaymentLink(NotificationRequest request) {
        logger.info("Sending payment link to user: {}, booking: {}",
                request.getUserId(), request.getBookingReference());

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String emailBody = String.format(
                "Your turn to book! Complete payment: %s\nExpires at: %s\nBooking: %s",
                request.getPaymentUrl(),
                request.getExpiredAt(),
                request.getBookingReference()
        );

        logger.info("📧 Email sent to user {}: {}", request.getUserId(), emailBody);

        String smsBody = String.format(
                "Payment ready! %s Complete within 15 min. Ref: %s",
                request.getPaymentUrl(),
                request.getBookingReference()
        );

        logger.info("📱 SMS sent to user {}: {}", request.getUserId(), smsBody);

        return CompletableFuture.completedFuture(true);
    }

    private String buildConfirmationEmail(NotificationRequest request) {
        return String.format(
                "✅ Booking Confirmed!\n\n" +
                        "Booking Reference: %s\n" +
                        "Ticket: %s\n" +
                        "Event: %s\n" +
                        "Amount: $%.2f\n" +
                        "Payment ID: %s\n" +
                        "Booked at: %s\n\n" +
                        "Thank you for your booking!",
                request.getBookingReference(),
                request.getTicketId(),
                request.getEventName(),
                request.getAmount(),
                request.getPaymentId(),
                Instant.now()
        );
    }

    private String buildConfirmationSms(NotificationRequest request) {
        return String.format(
                "Booking confirmed! Ref: %s. Ticket: %s. Amount: $%.2f",
                request.getBookingReference(),
                request.getTicketId(),
                request.getAmount()
        );
    }

    private String buildFailureEmail(NotificationRequest request) {
        return String.format(
                "❌ Booking Failed\n\n" +
                        "Booking Reference: %s\n" +
                        "Ticket: %s\n" +
                        "Amount: $%.2f\n" +
                        "Reason: %s\n\n" +
                        "Please try booking again or contact support.",
                request.getBookingReference(),
                request.getTicketId(),
                request.getAmount(),
                request.getFailureReason()
        );
    }

    private String buildFailureSms(NotificationRequest request) {
        return String.format(
                "Booking failed. Ref: %s. Reason: %s. Please try again.",
                request.getBookingReference(),
                request.getFailureReason()
        );
    }

    private String buildCancellationEmail(NotificationRequest request) {
        return String.format(
                "🔄 Booking Cancelled\n\n" +
                        "Booking Reference: %s\n" +
                        "Ticket: %s\n" +
                        "Refund Amount: $%.2f\n" +
                        "Refund ID: %s\n\n" +
                        "Your refund will be processed within 5-7 business days.",
                request.getBookingReference(),
                request.getTicketId(),
                request.getAmount(),
                request.getRefundId()
        );
    }

    private String buildCancellationSms(NotificationRequest request) {
        return String.format(
                "Booking cancelled. Ref: %s. Refund: $%.2f. Refund ID: %s",
                request.getBookingReference(),
                request.getAmount(),
                request.getRefundId()
        );
    }
}