package org.s4logic.payment.dto.enums;

/**
 * Payment Status Enum
 *
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    CANCELED,
    REFUNDED,
    PARTIALLY_REFUNDED
}
