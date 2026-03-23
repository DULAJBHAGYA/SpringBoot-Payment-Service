package org.s4logic.payment.mapper;

import org.s4logic.payment.dto.response.CheckoutSessionResponse;
import org.s4logic.payment.dto.response.PaymentIntentResponse;
import org.s4logic.payment.model.Payment;
import org.springframework.stereotype.Component;

/**
 * Payment Mapper
 *
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
@Component
public class PaymentMapper {
    /**
     * Convert Payment entity to PaymentIntentResponse DTO
     *
     * @param payment org.s4logic.payment.model.Payment
     * @return org.s4logic.payment.dto.response.PaymentIntentResponse
     */
    public PaymentIntentResponse toPaymentIntentResponse(Payment payment) {
        return PaymentIntentResponse.builder()
                .id(payment.getId())
                .paymentIntentId(payment.getPaymentIntentId())
                .clientSecret(payment.getClientSecret())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .description(payment.getDescription())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .customerEmail(payment.getCustomerEmail())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    /**
     * Convert Payment entity to CheckoutSessionResponse DTO
     *
     * @param payment    org.s4logic.payment.model.Payment
     * @param checkoutUrl String
     * @param expiresAt  java.time.OffsetDateTime
     * @return org.s4logic.payment.dto.response.CheckoutSessionResponse
     */
    public CheckoutSessionResponse toCheckoutSessionResponse(Payment payment, String checkoutUrl, java.time.OffsetDateTime expiresAt) {
        return CheckoutSessionResponse.builder()
                .sessionId(payment.getCheckoutSessionId())
                .checkoutUrl(checkoutUrl)
                .paymentIntentId(payment.getPaymentIntentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .customerEmail(payment.getCustomerEmail())
                .expiresAt(expiresAt)
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
