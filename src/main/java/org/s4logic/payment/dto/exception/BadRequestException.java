package org.s4logic.payment.dto.exception;

/**
 * Bad Request Exception
 * Used when the client sends invalid data or malformed requests
 *
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
