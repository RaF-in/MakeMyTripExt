package com.mmtext.bookingservice.service;

import com.mmtext.bookingservice.dto.*;
import com.mmtext.bookingservice.enums.Enums;
import com.mmtext.bookingservice.model.Booking;
import com.mmtext.bookingservice.model.Ticket;
import com.mmtext.bookingservice.repo.BookingRepository;
import com.mmtext.bookingservice.repo.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private TicketAvailabilityService ticketAvailabilityService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Main booking method - routes to appropriate handler
     */
    public BookingResponse createBooking(BookingRequest request) {
        logger.info("Creating booking for user: {}, ticket: {}, type: {}",
                request.getUserId(), request.getTicketId(), request.getConcurrencyType());

        // Verify ticket exists and is available
        if (!ticketAvailabilityService.isTicketAvailable(request.getTicketId())) {
            logger.warn("Ticket not available: {}", request.getTicketId());
            BookingResponse response = new BookingResponse();
            response.setMessage("We are sorry, this ticket is not available. Please select another ticket.");
            response.setStatus(Enums.BookingStatus.CANCELLED);
            return response;
        }

        switch (request.getConcurrencyType()) {
            case LOW:
                return handleLowConcurrencyBooking(request);
            case MEDIUM:
                return handleMediumConcurrencyBooking(request);
            case HIGH:
                return handleHighConcurrencyBooking(request);
            default:
                throw new IllegalArgumentException("Invalid concurrency type");
        }
    }

    /**
     * LOW CONCURRENCY: Hotel, Airline, Bus tickets
     * Everything stored in Redis until payment success
     */
    private BookingResponse handleLowConcurrencyBooking(BookingRequest request) {
        logger.info("Handling low concurrency booking for ticket: {}", request.getTicketId());

        Ticket ticket = ticketAvailabilityService.getTicket(request.getTicketId());

        // Call supplier to reserve
        SupplierReservationRequest supplierRequest = new SupplierReservationRequest(
                request.getTicketId(),
                request.getUserId()
        );

        SupplierReservationResponse supplierResponse = supplierService.reserveTicket(supplierRequest);

        if (!supplierResponse.isAvailable()) {
            logger.warn("Ticket not available from supplier: {}", request.getTicketId());
            BookingResponse response = new BookingResponse();
            response.setMessage("We are sorry, this ticket is not available. Please select another ticket.");
            response.setStatus(Enums.BookingStatus.CANCELLED);
            return response;
        }

        String bookingReference = generateBookingReference();
        Instant expiredAt = supplierResponse.getExpiredAt();
        long ttlSeconds = Duration.between(Instant.now(), expiredAt).getSeconds();

        // Lock ticket in Redis
        boolean locked = redisService.lockTicket(request.getTicketId(), bookingReference, ttlSeconds);

        if (!locked) {
            // Try to cancel supplier reservation
            supplierService.cancelReservation(supplierResponse.getReservationId());

            BookingResponse response = new BookingResponse();
            response.setMessage("We are sorry, this ticket is not available. Please select another ticket.");
            response.setStatus(Enums.BookingStatus.CANCELLED);
            return response;
        }

        // Store booking data in Redis
        BookingData bookingData = new BookingData(
                bookingReference,
                request.getUserId(),
                request.getTicketId(),
                ticket.getEventId(),
                Enums.ConcurrencyType.LOW,
                request.getAmount(),
                expiredAt
        );
        bookingData.setSupplierReservationId(supplierResponse.getReservationId());

        redisService.storeBookingData(bookingData, ttlSeconds);

        // Create payment session
        PaymentRequest paymentRequest = new PaymentRequest(
                bookingReference,
                request.getAmount(),
                request.getUserId()
        );

        PaymentResponse paymentResponse = paymentService.createPaymentSession(paymentRequest);

        // Update payment URL in Redis
        bookingData.setPaymentUrl(paymentResponse.getPaymentUrl());

        logger.info("Low concurrency booking created in Redis: {}", bookingReference);

        BookingResponse response = new BookingResponse(
                bookingReference,
                request.getTicketId(),
                Enums.BookingStatus.PAYMENT_PENDING,
                paymentResponse.getPaymentUrl(),
                expiredAt,
                "Please complete payment within the time limit"
        );

        return response;
    }

    /**
     * MEDIUM CONCURRENCY: Movie tickets
     * Everything in Redis until payment success
     */
    private BookingResponse handleMediumConcurrencyBooking(BookingRequest request) {
        logger.info("Handling medium concurrency booking for ticket: {}", request.getTicketId());

        Ticket ticket = ticketAvailabilityService.getTicket(request.getTicketId());

        String bookingReference = generateBookingReference();
        Instant expiredAt = Instant.now().plus(15, ChronoUnit.MINUTES);

        long ttlSeconds = Duration.between(Instant.now(), expiredAt).getSeconds();

        // Lock ticket atomically in Redis
        boolean locked = redisService.lockTicket(request.getTicketId(), bookingReference, ttlSeconds);

        if (!locked) {
            logger.warn("Failed to lock ticket: {}", request.getTicketId());
            BookingResponse response = new BookingResponse();
            response.setMessage("We are sorry, this ticket is not available. Please select another ticket.");
            response.setStatus(Enums.BookingStatus.CANCELLED);
            return response;
        }

        // Store booking data in Redis
        BookingData bookingData = new BookingData(
                bookingReference,
                request.getUserId(),
                request.getTicketId(),
                ticket.getEventId(),
                Enums.ConcurrencyType.MEDIUM,
                request.getAmount(),
                expiredAt
        );

        redisService.storeBookingData(bookingData, ttlSeconds);

        // Create payment session
        PaymentRequest paymentRequest = new PaymentRequest(
                bookingReference,
                request.getAmount(),
                request.getUserId()
        );

        PaymentResponse paymentResponse = paymentService.createPaymentSession(paymentRequest);

        // Update payment URL
        bookingData.setPaymentUrl(paymentResponse.getPaymentUrl());

        logger.info("Medium concurrency booking created in Redis: {}", bookingReference);

        BookingResponse response = new BookingResponse(
                bookingReference,
                request.getTicketId(),
                Enums.BookingStatus.PAYMENT_PENDING,
                paymentResponse.getPaymentUrl(),
                expiredAt,
                "Please complete payment within the time limit"
        );

        return response;
    }

    /**
     * HIGH CONCURRENCY: Concert tickets
     * Add to queue, store minimal data in DB for queue tracking
     */
    @Transactional
    protected BookingResponse handleHighConcurrencyBooking(BookingRequest request) {
        logger.info("Handling high concurrency booking for ticket: {}", request.getTicketId());

        String bookingReference = generateBookingReference();

        // Create minimal booking record for queue tracking only
        Booking booking = new Booking(
                bookingReference,
                request.getUserId(),
                request.getTicketId(),
                Enums.ConcurrencyType.HIGH,
                request.getAmount()
        );

        bookingRepository.save(booking);

        // Add to virtual queue
        redisService.addToQueue(request.getTicketId(), bookingReference);

        Long position = redisService.getQueuePosition(request.getTicketId(), bookingReference);
        Long queueSize = redisService.getQueueSize(request.getTicketId());

        logger.info("Added to queue: {}, Position: {}", bookingReference, position);

        BookingResponse response = new BookingResponse(
                bookingReference,
                request.getTicketId(),
                Enums.BookingStatus.QUEUED,
                null,
                null,
                "You are in the queue. You will be redirected to payment when it's your turn."
        );
        response.setQueuePosition(position != null ? position.intValue() + 1 : null);

        return response;
    }

    /**
     * Process payment webhook
     * On success, asynchronously update DB
     */
    public void handlePaymentWebhook(PaymentWebhookRequest webhookRequest) {
        logger.info("Processing payment webhook for booking: {}, Status: {}",
                webhookRequest.getBookingReference(), webhookRequest.getStatus());

        if ("success".equalsIgnoreCase(webhookRequest.getStatus())) {
            // Asynchronously confirm booking in DB
            confirmBookingAsync(webhookRequest);
        } else {
            // Payment failed, cleanup Redis
            cleanupFailedBooking(webhookRequest.getBookingReference());
        }
    }

    /**
     * Get booking details
     */
    public BookingDetailsResponse getBookingDetails(String bookingReference) {
        // Check Redis first
        BookingData bookingData = redisService.getBookingData(bookingReference);

        if (bookingData != null) {
            BookingDetailsResponse response = new BookingDetailsResponse();
            response.setBookingReference(bookingReference);
            response.setUserId(bookingData.getUserId());
            response.setTicketId(bookingData.getTicketId());
            response.setStatus(Enums.BookingStatus.PAYMENT_PENDING);
            response.setConcurrencyType(bookingData.getConcurrencyType());
            response.setAmount(bookingData.getAmount());
            response.setExpiredAt(bookingData.getExpiredAt());
            return response;
        }

        // Check DB
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        BookingDetailsResponse response = new BookingDetailsResponse();
        response.setBookingReference(booking.getBookingReference());
        response.setUserId(booking.getUserId());
        response.setTicketId(booking.getTicketId());
        response.setStatus(booking.getStatus());
        response.setConcurrencyType(booking.getConcurrencyType());
        response.setAmount(booking.getAmount());
        response.setPaidAt(booking.getPaidAt());
        response.setPaymentId(booking.getPaymentId());

        return response;
    }

    /**
     * Generate unique booking reference
     */
    private String generateBookingReference() {
        return "BK-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


    @Async
    @Transactional
    public void confirmBookingAsync(PaymentWebhookRequest webhookRequest) {
        try {
            String bookingReference = webhookRequest.getBookingReference();

            // Get booking data from Redis
            BookingData bookingData = redisService.getBookingData(bookingReference);

            if (bookingData == null) {
                logger.error("Booking data not found in Redis: {}", bookingReference);
                return;
            }

            // Get ticket details for notification
            Ticket ticket = ticketRepository.findByTicketId(bookingData.getTicketId())
                    .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

            // Update ticket status in DB
            ticket.setStatus(Enums.TicketStatus.BOOKED);
            ticket.setBookedByUserId(bookingData.getUserId());
            ticket.setBookingReference(bookingReference);
            ticket.setBookedAt(Instant.now());
            ticketRepository.save(ticket);

            // Create confirmed booking record in DB
            Booking booking = new Booking(
                    bookingReference,
                    bookingData.getUserId(),
                    bookingData.getTicketId(),
                    bookingData.getConcurrencyType(),
                    Enums.BookingStatus.CONFIRMED,
                    bookingData.getAmount()
            );
            booking.setPaymentId(webhookRequest.getPaymentId());
            booking.setPaidAt(webhookRequest.getPaidAt());
            bookingRepository.save(booking);

            // Cleanup Redis
            redisService.deleteBookingData(bookingReference);
            redisService.unlockTicket(bookingData.getTicketId());
            redisService.removeFromProcessing(bookingReference);

            // Confirm with supplier if LOW concurrency
            if (bookingData.getConcurrencyType() == Enums.ConcurrencyType.LOW &&
                    bookingData.getSupplierReservationId() != null) {
                supplierService.confirmBooking(
                        bookingData.getSupplierReservationId(),
                        webhookRequest.getPaymentId()
                );
            }

            logger.info("Booking confirmed asynchronously in DB: {}", bookingReference);

            // Send confirmation notification
            NotificationRequest notificationRequest = new NotificationRequest(
                    bookingData.getUserId(),
                    bookingReference,
                    bookingData.getTicketId(),
                    ticket.getEventName(),
                    bookingData.getAmount()
            );
            notificationRequest.setPaymentId(webhookRequest.getPaymentId());

            notificationService.sendBookingConfirmation(notificationRequest);

        } catch (Exception e) {
            logger.error("Failed to confirm booking asynchronously", e);
        }
    }

// Update the cleanupFailedBooking method to include notification:

    private void cleanupFailedBooking(String bookingReference) {
        BookingData bookingData = redisService.getBookingData(bookingReference);

        if (bookingData != null) {
            // Unlock ticket
            redisService.unlockTicket(bookingData.getTicketId());

            // Cancel supplier reservation if LOW concurrency
            if (bookingData.getConcurrencyType() == Enums.ConcurrencyType.LOW &&
                    bookingData.getSupplierReservationId() != null) {
                supplierService.cancelReservation(bookingData.getSupplierReservationId());
            }

            // Get ticket for notification
            Ticket ticket = ticketRepository.findByTicketId(bookingData.getTicketId()).orElse(null);

            // Send failure notification
            NotificationRequest notificationRequest = new NotificationRequest(
                    bookingData.getUserId(),
                    bookingReference,
                    bookingData.getTicketId(),
                    ticket != null ? ticket.getEventName() : "Unknown Event",
                    bookingData.getAmount()
            );
            notificationRequest.setFailureReason("Payment failed or expired");

            notificationService.sendBookingFailure(notificationRequest);

            // Delete from Redis
            redisService.deleteBookingData(bookingReference);
        }

        redisService.removeFromProcessing(bookingReference);

        logger.info("Cleaned up failed booking: {}", bookingReference);
    }

// Update the cancelBooking method to include notification:

    @Transactional
    public CancelBookingResponse cancelBooking(CancelBookingRequest request) {
        logger.info("Cancelling booking: {}", request.getBookingReference());

        // Check Redis first
        BookingData bookingData = redisService.getBookingData(request.getBookingReference());

        if (bookingData != null) {
            // Booking in payment pending state, just cleanup Redis
            redisService.unlockTicket(bookingData.getTicketId());
            redisService.deleteBookingData(request.getBookingReference());

            if (bookingData.getConcurrencyType() == Enums.ConcurrencyType.LOW &&
                    bookingData.getSupplierReservationId() != null) {
                supplierService.cancelReservation(bookingData.getSupplierReservationId());
            }

            // Get ticket for notification
            Ticket ticket = ticketRepository.findByTicketId(bookingData.getTicketId()).orElse(null);

            // Send cancellation notification
            NotificationRequest notificationRequest = new NotificationRequest(
                    bookingData.getUserId(),
                    request.getBookingReference(),
                    bookingData.getTicketId(),
                    ticket != null ? ticket.getEventName() : "Unknown Event",
                    bookingData.getAmount()
            );

            notificationService.sendCancellationNotification(notificationRequest);

            return new CancelBookingResponse(
                    request.getBookingReference(),
                    Enums.BookingStatus.CANCELLED,
                    "Booking cancelled successfully"
            );
        }

        // Check DB for confirmed booking
        Booking booking = bookingRepository.findByBookingReferenceAndUserId(
                request.getBookingReference(),
                request.getUserId()
        ).orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() == Enums.BookingStatus.CONFIRMED) {
            // Process refund
            String refundId = paymentService.processRefund(
                    booking.getPaymentId(),
                    booking.getAmount()
            );

            // Update ticket status
            Ticket ticket = ticketRepository.findByTicketId(booking.getTicketId())
                    .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

            ticket.setStatus(Enums.TicketStatus.AVAILABLE);
            ticket.setBookedByUserId(null);
            ticket.setBookingReference(null);
            ticket.setBookedAt(null);
            ticketRepository.save(ticket);

            // Update booking status
            booking.setStatus(Enums.BookingStatus.CANCELLED);
            booking.setCancelledAt(Instant.now());
            bookingRepository.save(booking);

            // Send cancellation notification
            NotificationRequest notificationRequest = new NotificationRequest(
                    booking.getUserId(),
                    request.getBookingReference(),
                    booking.getTicketId(),
                    ticket.getEventName(),
                    booking.getAmount()
            );
            notificationRequest.setRefundId(refundId);

            notificationService.sendCancellationNotification(notificationRequest);

            CancelBookingResponse response = new CancelBookingResponse(
                    booking.getBookingReference(),
                    Enums.BookingStatus.CANCELLED,
                    "Booking cancelled and refund initiated"
            );
            response.setRefundId(refundId);

            return response;
        }

        return new CancelBookingResponse(
                request.getBookingReference(),
                Enums.BookingStatus.CANCELLED,
                "Booking already cancelled"
        );
    }
}