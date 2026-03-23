package org.s4logic.payment.dto.exception;

/**
 * Business Logic Exception
 * Used when business rules or constraints are violated
 *
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
public class BusinessLogicException extends RuntimeException {
    public BusinessLogicException(String message) {
        super(message);
    }

    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }
}
