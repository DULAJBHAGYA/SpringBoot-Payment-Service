package org.s4logic.payment.dto.response;

import lombok.*;
import org.s4logic.payment.dto.enums.Currency;
import org.s4logic.payment.dto.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Checkout Session Response
 * Response containing Stripe Checkout Session details
 *
 * @author Prasad Madusanka
 * @since 29 January 2026
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutSessionResponse {
    private String sessionId;
    private String checkoutUrl;
    private String paymentIntentId;
    private BigDecimal amount;
    private Currency currency;
    private PaymentStatus status;
    private String orderId;
    private String customerId;
    private String customerEmail;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
}
