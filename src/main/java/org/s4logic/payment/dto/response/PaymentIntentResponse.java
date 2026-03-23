package org.s4logic.payment.dto.response;

import lombok.*;
import org.s4logic.payment.dto.enums.Currency;
import org.s4logic.payment.dto.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Payment Intent Response DTO
 *
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentIntentResponse {
    private Long id;
    private String paymentIntentId;
    private String clientSecret;
    private BigDecimal amount;
    private Currency currency;
    private PaymentStatus status;
    private String description;
    private String orderId;
    private String customerId;
    private String customerEmail;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
