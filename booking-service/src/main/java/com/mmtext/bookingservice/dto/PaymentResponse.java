package com.mmtext.bookingservice.dto;

public class PaymentResponse {

    private String paymentId;
    private String paymentUrl;
    private String status;

    public PaymentResponse() {}

    public PaymentResponse(String paymentId, String paymentUrl, String status) {
        this.paymentId = paymentId;
        this.paymentUrl = paymentUrl;
        this.status = status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
