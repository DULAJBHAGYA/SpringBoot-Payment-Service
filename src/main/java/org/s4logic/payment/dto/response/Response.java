package org.s4logic.payment.dto.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Generic Response DTO
 *
 * @param <T> Type of the data payload
 * @author Prasad Madusanka
 * @since 25 January 2026
 */
@Getter
@Setter
public class Response<T> {
    private int status;
    private String message;
    private T data;
    private OffsetDateTime timestamp;
    private List<ValidationError> errors;

    public Response(HttpStatus status, String message) {
        this.status = status.value();
        this.message = message;
        this.timestamp = OffsetDateTime.now();
    }

    public Response(HttpStatus status, String message, List<ValidationError> errors) {
        this.status = status.value();
        this.message = message;
        this.errors = errors;
        this.timestamp = OffsetDateTime.now();
    }

    public Response(HttpStatus status, String message, T data) {
        this.status = status.value();
        this.message = message;
        this.data = data;
        this.timestamp = OffsetDateTime.now();
    }

    @Getter
    @Setter
    public static class ValidationError {
        private String field;
        private String message;

        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
