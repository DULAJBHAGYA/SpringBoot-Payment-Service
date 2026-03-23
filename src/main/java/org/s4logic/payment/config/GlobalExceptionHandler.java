package org.s4logic.payment.config;

import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.s4logic.payment.dto.exception.BadRequestException;
import org.s4logic.payment.dto.exception.BusinessLogicException;
import org.s4logic.payment.dto.exception.DuplicateResourceException;
import org.s4logic.payment.dto.exception.ResourceNotFoundException;
import org.s4logic.payment.dto.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Global Exception Handler
 * Centralized exception handling for all controllers
 * Maps exceptions to appropriate HTTP status codes
 *
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * Handle Resource Not Found exceptions
     * Returns 404 NOT FOUND
     */
    @ExceptionHandler({ResourceNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<Response<Void>> handleResourceNotFoundException(Exception ex) {
        log.error("Resource not found: {}", ex.getMessage());
        Response<Void> response = new Response<>(HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle Duplicate Resource exceptions
     * Returns 409 CONFLICT
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Response<Void>> handleDuplicateResourceException(DuplicateResourceException ex) {
        log.error("Duplicate resource: {}", ex.getMessage());
        Response<Void> response = new Response<>(HttpStatus.CONFLICT, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handle Bad Request exceptions
     * Returns 400 BAD REQUEST
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Response<Void>> handleBadRequestException(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage());
        Response<Void> response = new Response<>(HttpStatus.BAD_REQUEST, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Business Logic exceptions
     * Returns 422 UNPROCESSABLE ENTITY
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<Response<Void>> handleBusinessLogicException(BusinessLogicException ex) {
        log.error("Business logic error: {}", ex.getMessage());
        Response<Void> response = new Response<>(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Handle Validation exceptions
     * Returns 400 BAD REQUEST with validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        List<Response.ValidationError> validationErrors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(new Response.ValidationError(error.getField(), error.getDefaultMessage()));
        }
        Response<Void> response = new Response<>(HttpStatus.BAD_REQUEST, "Validation failed", validationErrors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Type Mismatch exceptions
     * Returns 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch: {}", ex.getMessage());
        String typeName = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", ex.getValue(), ex.getName(), typeName);
        Response<Void> response = new Response<>(HttpStatus.BAD_REQUEST, message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle Stripe API exceptions
     * Returns 422 UNPROCESSABLE ENTITY
     */
    @ExceptionHandler(StripeException.class)
    public ResponseEntity<Response<Void>> handleStripeException(StripeException ex) {
        log.error("Stripe API error: {} - {}", ex.getCode(), ex.getMessage(), ex);
        String message = String.format("Payment processing error: %s", ex.getUserMessage() != null ? ex.getUserMessage() : ex.getMessage());
        Response<Void> response = new Response<>(HttpStatus.UNPROCESSABLE_ENTITY, message);
        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Handle all other exceptions
     * Returns 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleGlobalException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        Response<Void> response = new Response<>(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
