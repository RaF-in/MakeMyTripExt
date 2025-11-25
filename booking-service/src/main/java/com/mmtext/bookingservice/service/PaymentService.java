package com.mmtext.bookingservice.service;

import com.mmtext.bookingservice.dto.PaymentRequest;
import com.mmtext.bookingservice.dto.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private static final String PAYMENT_BASE_URL = "https://payment.example.com/checkout/";

    /**
     * Create payment session (simulating Stripe checkout session)
     * This is a dummy implementation
     */
    public PaymentResponse createPaymentSession(PaymentRequest request) {
        logger.info("Creating payment session for booking: {}", request.getBookingReference());

        // Simulate processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String paymentId = "PAY-" + UUID.randomUUID().toString();
        String paymentUrl = PAYMENT_BASE_URL + paymentId;

        logger.info("Payment session created. PaymentId: {}, Amount: {}",
                paymentId, request.getAmount());

        return new PaymentResponse(paymentId, paymentUrl, "pending");
    }

    /**
     * Process refund (simulating Stripe refund)
     */
    public String processRefund(String paymentId, Double amount) {
        logger.info("Processing refund for payment: {}, Amount: {}", paymentId, amount);

        // Simulate processing delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String refundId = "REF-" + UUID.randomUUID().toString();

        logger.info("Refund processed successfully. RefundId: {}", refundId);

        return refundId;
    }

    /**
     * Verify payment status
     */
    public boolean verifyPayment(String paymentId) {
        logger.info("Verifying payment: {}", paymentId);

        // In real implementation, this would call Stripe API
        // For dummy, we'll just return true
        return true;
    }
}