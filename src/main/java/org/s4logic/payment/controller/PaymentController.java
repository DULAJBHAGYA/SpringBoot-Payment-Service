package org.s4logic.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.s4logic.payment.dto.request.CreateCheckoutSessionRequest;
import org.s4logic.payment.dto.request.RefundRequest;
import org.s4logic.payment.dto.response.CheckoutSessionResponse;
import org.s4logic.payment.dto.response.PaymentIntentResponse;
import org.s4logic.payment.dto.response.RefundResponse;
import org.s4logic.payment.dto.response.Response;
import org.s4logic.payment.service.StripePaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Payment Controller
 * <p>
 * Handles Stripe Checkout payment operations including creating checkout sessions,
 * verifying payments, processing refunds, and managing payment cancellations.
 *
 * @author Prasad Madusanka
 * @since 29 January 2026
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/payment")
public class PaymentController {
    private final StripePaymentService stripePaymentService;

    // ========== STRIPE CHECKOUT ENDPOINTS ==========

    /**
     * Create a Stripe Checkout Session
     * This returns a URL to redirect the user to Stripe's hosted payment page
     * POST /v1/payment/checkout/session
     *
     * @param request org.s4logic.payment.dto.request.CreateCheckoutSessionRequest
     * @return org.springframework.http.ResponseEntity<org.s4logic.payment.dto.response.Response<org.s4logic.payment.dto.response.CheckoutSessionResponse>>
     */
    @PostMapping("/checkout/session")
    public ResponseEntity<Response<CheckoutSessionResponse>> createCheckoutSession(@Valid @RequestBody CreateCheckoutSessionRequest request) {
        CheckoutSessionResponse checkoutSession = stripePaymentService.createCheckoutSession(request);
        Response<CheckoutSessionResponse> response = new Response<>(HttpStatus.CREATED, "Checkout session created successfully. Redirect user to checkoutUrl.", checkoutSession);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieve checkout session status
     * Call this endpoint when user returns from Stripe's checkout page
     * GET /v1/payment/checkout/session/{sessionId}
     *
     * @param sessionId String
     * @return org.springframework.http.ResponseEntity<org.s4logic.payment.dto.response.Response<org.s4logic.payment.dto.response.PaymentIntentResponse>>
     */
    @GetMapping("/checkout/session/{sessionId}")
    public ResponseEntity<Response<PaymentIntentResponse>> retrieveCheckoutSession(@PathVariable String sessionId) {
        PaymentIntentResponse payment = stripePaymentService.retrieveCheckoutSession(sessionId);
        Response<PaymentIntentResponse> response = new Response<>(HttpStatus.OK, "Checkout session retrieved successfully", payment);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify checkout session payment completion
     * Use this on your success page to verify the payment was actually completed
     * GET /v1/payment/checkout/session/{sessionId}/verify
     *
     * @param sessionId String
     * @return org.springframework.http.ResponseEntity<org.s4logic.payment.dto.response.Response<java.util.Map<String, Boolean>>>
     */
    @GetMapping("/checkout/session/{sessionId}/verify")
    public ResponseEntity<Response<Map<String, Boolean>>> verifyCheckoutSession(@PathVariable String sessionId) {
        boolean verified = stripePaymentService.verifyCheckoutSession(sessionId);
        Response<Map<String, Boolean>> response = new Response<>(HttpStatus.OK, verified ? "Payment completed successfully" : "Payment not completed", Map.of("verified", verified, "paymentSucceeded", verified));
        return ResponseEntity.ok(response);
    }

    // ========== PAYMENT MANAGEMENT ENDPOINTS ==========

    /**
     * Cancel a payment
     * POST /v1/payment/{paymentIntentId}/cancel
     *
     * @param paymentIntentId String - The Stripe payment intent ID
     * @return org.springframework.http.ResponseEntity<org.s4logic.payment.dto.response.Response<org.s4logic.payment.dto.response.PaymentIntentResponse>>
     */
    @PostMapping("/{paymentIntentId}/cancel")
    public ResponseEntity<Response<PaymentIntentResponse>> cancelPayment(@PathVariable String paymentIntentId) {
        PaymentIntentResponse payment = stripePaymentService.cancelPayment(paymentIntentId);
        Response<PaymentIntentResponse> response = new Response<>(HttpStatus.OK, "Payment canceled successfully", payment);
        return ResponseEntity.ok(response);
    }

    /**
     * Refund a payment (full or partial)
     * POST /v1/payment/{paymentIntentId}/refund
     *
     * @param paymentIntentId String - The Stripe payment intent ID
     * @param request         org.s4logic.payment.dto.request.RefundRequest
     * @return org.springframework.http.ResponseEntity<org.s4logic.payment.dto.response.Response<org.s4logic.payment.dto.response.RefundResponse>>
     */
    @PostMapping("/{paymentIntentId}/refund")
    public ResponseEntity<Response<RefundResponse>> refundPayment(@PathVariable String paymentIntentId, @RequestBody RefundRequest request) {
        RefundResponse refund = stripePaymentService.refundPayment(paymentIntentId, request);
        String message = "already_refunded".equalsIgnoreCase(refund.getStatus())
                ? "Payment is already fully refunded"
                : "Payment refunded successfully";
        Response<RefundResponse> response = new Response<>(HttpStatus.OK, message, refund);
        return ResponseEntity.ok(response);
    }

    // ========== QUERY ENDPOINTS ==========

    /**
     * Get all payments for a customer
     * GET /v1/payment/customer/{customerId}
     *
     * @param customerId String
     * @return org.springframework.http.ResponseEntity<org.s4logic.payment.dto.response.Response<java.util.List<org.s4logic.payment.dto.response.PaymentIntentResponse>>>
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Response<List<PaymentIntentResponse>>> getCustomerPayments(@PathVariable String customerId) {
        List<PaymentIntentResponse> payments = stripePaymentService.getCustomerPayments(customerId);
        Response<List<PaymentIntentResponse>> response = new Response<>(HttpStatus.OK, "Customer payments retrieved successfully", payments);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all payments for an order
     * GET /v1/payment/order/{orderId}
     *
     * @param orderId String
     * @return org.springframework.http.ResponseEntity<org.s4logic.payment.dto.response.Response<java.util.List<org.s4logic.payment.dto.response.PaymentIntentResponse>>>
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Response<List<PaymentIntentResponse>>> getOrderPayments(@PathVariable String orderId) {
        List<PaymentIntentResponse> payments = stripePaymentService.getOrderPayments(orderId);
        Response<List<PaymentIntentResponse>> response = new Response<>(HttpStatus.OK, "Order payments retrieved successfully", payments);
        return ResponseEntity.ok(response);
    }


}
