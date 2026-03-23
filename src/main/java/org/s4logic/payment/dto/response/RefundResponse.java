package org.s4logic.payment.dto.response;

import lombok.*;
import org.s4logic.payment.dto.enums.Currency;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Refund Response DTO
 *
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {
    private String refundId;
    private String paymentIntentId;
    private BigDecimal amount;
    private Currency currency;
    private String status;
    private String reason;
    private OffsetDateTime createdAt;
}
