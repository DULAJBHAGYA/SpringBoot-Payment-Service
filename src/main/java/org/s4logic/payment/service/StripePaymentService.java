package org.s4logic.payment.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCancelParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.s4logic.payment.dto.enums.PaymentMethod;
import org.s4logic.payment.dto.enums.PaymentStatus;
import org.s4logic.payment.dto.exception.BadRequestException;
import org.s4logic.payment.dto.exception.BusinessLogicException;
import org.s4logic.payment.dto.exception.ResourceNotFoundException;
import org.s4logic.payment.dto.request.CreateCheckoutSessionRequest;
import org.s4logic.payment.dto.request.RefundRequest;
import org.s4logic.payment.dto.response.CheckoutSessionResponse;
import org.s4logic.payment.dto.response.PaymentIntentResponse;
import org.s4logic.payment.dto.response.RefundResponse;
import org.s4logic.payment.mapper.PaymentMapper;
import org.s4logic.payment.model.Payment;
import org.s4logic.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stripe Payment Service
 * <p>
 * Handles payment operations using Stripe Checkout (hosted payment page).
 *
 * @author Prasad Madusanka
 * @since 29 January 2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {
    private static final String PAYMENT_NOT_FOUND_MSG = "Payment not found";

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    /**
     * Create a Stripe Checkout Session (hosted payment page)
     * This is the recommended approach that redirects users to Stripe's hosted checkout page
     *
     * @param request org.s4logic.payment.dto.request.CreateCheckoutSessionRequest
     * @return org.s4logic.payment.dto.response.CheckoutSessionResponse
     */
    @Transactional
    public CheckoutSessionResponse createCheckoutSession(CreateCheckoutSessionRequest request) {
        log.info("Creating checkout session for amount: {} {}", request.getAmount(), request.getCurrency());

        try {
            // Convert amount to cents (Stripe expects smallest currency unit)
            Long amountInCents = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

            // Build metadata
            Map<String, String> metadata = new HashMap<>();
            if (request.getOrderId() != null) {
                metadata.put("orderId", request.getOrderId());
            }
            if (request.getCustomerId() != null) {
                metadata.put("customerId", request.getCustomerId());
            }

            // Build line item
            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(request.getCurrency().name().toLowerCase())
                                    .setUnitAmount(amountInCents)
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(request.getProductName())
                                                    .setDescription(request.getProductDescription())
                                                    .build()
                                    )
                                    .build()
                    )
                    .setQuantity(Long.valueOf(request.getQuantity()))
                    .build();

            // Build session parameters
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .addLineItem(lineItem)
                    .setSuccessUrl(request.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(request.getCancelUrl())
                    .putAllMetadata(metadata);

            if (request.getCustomerEmail() != null) {
                paramsBuilder.setCustomerEmail(request.getCustomerEmail());
            }

            // Create checkout session in Stripe
            Session session = Session.create(paramsBuilder.build());

            // Save payment to database
            Payment payment = new Payment();
            payment.setCheckoutSessionId(session.getId());
            payment.setPaymentIntentId(session.getPaymentIntent());
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setDescription(request.getProductDescription());
            payment.setOrderId(request.getOrderId());
            payment.setCustomerId(request.getCustomerId());
            payment.setCustomerEmail(request.getCustomerEmail());
            payment.setRefundedAmount(BigDecimal.ZERO);

            payment = paymentRepository.save(payment);

            log.info("Checkout session created successfully: {}", session.getId());

            // Convert expiry timestamp to OffsetDateTime
            OffsetDateTime expiresAt = OffsetDateTime.ofInstant(Instant.ofEpochSecond(session.getExpiresAt()), ZoneOffset.UTC);

            return paymentMapper.toCheckoutSessionResponse(payment, session.getUrl(), expiresAt);
        } catch (StripeException e) {
            log.error("Stripe error while creating checkout session: {}", e.getMessage(), e);
            throw new BusinessLogicException("Failed to create checkout session: " + e.getMessage());
        }
    }

    /**
     * Retrieve checkout session status
     * Call this method after user returns from Stripe's checkout page
     *
     * @param sessionId String
     * @return org.s4logic.payment.dto.response.PaymentIntentResponse
     */
    @Transactional
    public PaymentIntentResponse retrieveCheckoutSession(String sessionId) {
        log.info("Retrieving checkout session: {}", sessionId);

        Payment payment = paymentRepository.findByCheckoutSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout session not found"));
        try {
            // Retrieve session from Stripe
            Session session = Session.retrieve(sessionId);

            // Update payment with latest information
            if (session.getPaymentIntent() != null) {
                payment.setPaymentIntentId(session.getPaymentIntent());

                // Retrieve payment intent for detailed status
                PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
                updatePaymentFromStripeIntent(payment, paymentIntent);
            } else {
                // Update status based on session payment status
                updatePaymentFromSessionStatus(payment, session.getPaymentStatus());
            }

            payment = paymentRepository.save(payment);
            log.info("Checkout session retrieved: {} with status: {}", sessionId, payment.getStatus());

            return paymentMapper.toPaymentIntentResponse(payment);
        } catch (StripeException e) {
            log.error("Stripe error while retrieving checkout session: {}", e.getMessage(), e);
            throw new BusinessLogicException("Failed to retrieve checkout session: " + e.getMessage());
        }
    }

    /**
     * Verify checkout session payment completion
     * Use this on your success page to verify the payment was actually completed
     *
     * @param sessionId String
     * @return boolean true if payment succeeded, false otherwise
     */
    @Transactional
    public boolean verifyCheckoutSession(String sessionId) {
        log.info("Verifying checkout session: {}", sessionId);

        PaymentIntentResponse payment = retrieveCheckoutSession(sessionId);
        return payment.getStatus() == PaymentStatus.SUCCEEDED;
    }

    /**
     * Cancel a payment (synchronous)
     * Works for checkout session payments
     *
     * @param paymentIntentId String
     * @return org.s4logic.payment.dto.response.PaymentIntentResponse
     */
    @Transactional
    public PaymentIntentResponse cancelPayment(String paymentIntentId) {
        log.info("Canceling payment: {}", paymentIntentId);

        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException(PAYMENT_NOT_FOUND_MSG));
        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            throw new BadRequestException("Cannot cancel a succeeded payment. Use refund instead.");
        }
        if (payment.getStatus() == PaymentStatus.CANCELED) {
            throw new BadRequestException("Payment is already canceled");
        }

        try {
            // Cancel payment intent in Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            PaymentIntentCancelParams params = PaymentIntentCancelParams.builder().build();
            paymentIntent.cancel(params);

            // Update payment status
            payment.setStatus(PaymentStatus.CANCELED);
            payment = paymentRepository.save(payment);

            log.info("Payment canceled successfully: {}", paymentIntentId);

            return paymentMapper.toPaymentIntentResponse(payment);
        } catch (StripeException e) {
            log.error("Stripe error while canceling payment: {}", e.getMessage(), e);
            throw new BusinessLogicException("Failed to cancel payment: " + e.getMessage());
        }
    }

    /**
     * Refund a payment (synchronous)
     * Supports full and partial refunds
     *
     * @param paymentIntentId String
     * @param request         org.s4logic.payment.dto.request.RefundRequest
     * @return org.s4logic.payment.dto.response.RefundResponse
     */
    @Transactional
    public RefundResponse refundPayment(String paymentIntentId, RefundRequest request) {
        log.info("Refunding payment: {}", paymentIntentId);

        Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResourceNotFoundException(PAYMENT_NOT_FOUND_MSG));
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            log.info("Payment already fully refunded: {}", paymentIntentId);
            return RefundResponse.builder()
                    .refundId("already_refunded")
                    .paymentIntentId(paymentIntentId)
                    .amount(payment.getRefundedAmount())
                    .currency(payment.getCurrency())
                    .status("already_refunded")
                    .reason("Payment is already fully refunded")
                    .createdAt(OffsetDateTime.now())
                    .build();
        }
        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new BadRequestException("Can only refund succeeded payments");
        }

        try {
            // Build refund parameters
            RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder().setPaymentIntent(paymentIntentId);
            if (request.getAmount() != null) {
                // Partial refund
                Long refundAmountInCents = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
                paramsBuilder.setAmount(refundAmountInCents);
            }
            if (request.getReason() != null) {
                paramsBuilder.setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
            }

            // Create refund in Stripe
            Refund refund = Refund.create(paramsBuilder.build());

            // Update payment status
            BigDecimal refundAmount = request.getAmount() != null ? request.getAmount() : payment.getAmount();
            BigDecimal totalRefunded = payment.getRefundedAmount().add(refundAmount);
            payment.setRefundedAmount(totalRefunded);
            if (totalRefunded.compareTo(payment.getAmount()) >= 0) {
                payment.setStatus(PaymentStatus.REFUNDED);
            } else {
                payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
            }

            paymentRepository.save(payment);

            log.info("Refund created successfully: {}", refund.getId());

            return RefundResponse.builder()
                    .refundId(refund.getId())
                    .paymentIntentId(paymentIntentId)
                    .amount(refundAmount)
                    .currency(payment.getCurrency())
                    .status(refund.getStatus())
                    .reason(request.getReason())
                    .createdAt(OffsetDateTime.now())
                    .build();
        } catch (StripeException e) {
            log.error("Stripe error while refunding payment: {}", e.getMessage(), e);
            throw new BusinessLogicException("Failed to refund payment: " + e.getMessage());
        }
    }

    /**
     * Get all payments for a customer
     *
     * @param customerId String
     * @return java.util.List<org.s4logic.payment.dto.response.PaymentIntentResponse>
     */
    public List<PaymentIntentResponse> getCustomerPayments(String customerId) {
        log.info("Retrieving payments for customer: {}", customerId);
        return paymentRepository.findByCustomerId(customerId).stream().map(paymentMapper::toPaymentIntentResponse).toList();
    }

    /**
     * Get all payments for an order
     *
     * @param orderId String
     * @return java.util.List<org.s4logic.payment.dto.response.PaymentIntentResponse>
     */
    public List<PaymentIntentResponse> getOrderPayments(String orderId) {
        log.info("Retrieving payments for order: {}", orderId);
        return paymentRepository.findByOrderId(orderId).stream().map(paymentMapper::toPaymentIntentResponse).toList();
    }

    /**
     * Helper method to update payment from Stripe PaymentIntent
     *
     * @param payment       org.s4logic.payment.model.Payment
     * @param paymentIntent com.stripe.model.PaymentIntent
     */
    private void updatePaymentFromStripeIntent(Payment payment, PaymentIntent paymentIntent) {
        String stripeStatus = paymentIntent.getStatus();
        switch (stripeStatus) {
            case "succeeded":
                payment.setStatus(PaymentStatus.SUCCEEDED);
                payment.setPaymentMethod(determinePaymentMethod(paymentIntent.getPaymentMethodTypes()));
                break;
            case "processing", "requires_capture":
                payment.setStatus(PaymentStatus.PROCESSING);
                break;
            case "requires_payment_method", "requires_confirmation", "requires_action":
                payment.setStatus(PaymentStatus.PENDING);
                break;
            case "canceled":
                payment.setStatus(PaymentStatus.CANCELED);
                break;
            default:
                payment.setStatus(PaymentStatus.FAILED);
                break;
        }
        if (paymentIntent.getLastPaymentError() != null) {
            payment.setFailureMessage(paymentIntent.getLastPaymentError().getMessage());
        }
    }

    /**
     * Helper method to determine payment method from payment method types
     *
     * @param paymentMethodTypes java.util.List<String>
     * @return org.s4logic.payment.dto.enums.PaymentMethod
     */
    private PaymentMethod determinePaymentMethod(List<String> paymentMethodTypes) {
        if (paymentMethodTypes != null && !paymentMethodTypes.isEmpty()) {
            String type = paymentMethodTypes.get(0);
            if (type.contains("card")) {
                return PaymentMethod.CARD;
            } else if (type.contains("bank") || type.contains("ach") || type.contains("sepa")) {
                return PaymentMethod.BANK_TRANSFER;
            }
        }
        return PaymentMethod.CARD; // Default
    }

    /**
     * Helper method to update payment status from Stripe Checkout Session payment status
     *
     * @param payment       org.s4logic.payment.model.Payment
     * @param paymentStatus String
     */
    private void updatePaymentFromSessionStatus(Payment payment, String paymentStatus) {
        if (paymentStatus == null) {
            return;
        }
        switch (paymentStatus) {
            case "paid":
                payment.setStatus(PaymentStatus.SUCCEEDED);
                break;
            case "unpaid":
                payment.setStatus(PaymentStatus.PENDING);
                break;
            case "no_payment_required":
                payment.setStatus(PaymentStatus.SUCCEEDED);
                break;
            default:
                payment.setStatus(PaymentStatus.PENDING);
                break;
        }
    }
}
