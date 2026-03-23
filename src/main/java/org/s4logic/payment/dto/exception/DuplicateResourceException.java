package org.s4logic.payment.dto.exception;

import lombok.Getter;

/**
 * Duplicate Resource Exception
 * Used when attempting to create a resource that already exists
 *
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
@Getter
public class DuplicateResourceException extends RuntimeException {
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public DuplicateResourceException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }
}
