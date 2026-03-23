package org.s4logic.payment.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.s4logic.payment.dto.enums.Currency;

import java.math.BigDecimal;

/**
 * Create Checkout Session Request
 * Request to create a Stripe Checkout Session
 *
 * @author Prasad Madusanka
 * @since 29 January 2026
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCheckoutSessionRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private Currency currency;

    @NotBlank(message = "Product name is required")
    private String productName;

    private String productDescription;

    @Builder.Default
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    private String orderId;

    private String customerId;

    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Success URL is required")
    private String successUrl;

    @NotBlank(message = "Cancel URL is required")
    private String cancelUrl;
}
