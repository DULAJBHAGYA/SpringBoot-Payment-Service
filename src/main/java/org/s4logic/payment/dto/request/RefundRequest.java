package org.s4logic.payment.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Refund Request DTO
 *
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    @Positive(message = "Refund amount must be positive")
    private BigDecimal amount; // Optional: if null, full refund
    private String reason;
}
