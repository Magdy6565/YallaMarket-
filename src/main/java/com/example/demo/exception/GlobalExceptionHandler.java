// src/main/java/com/example/demo/exception/GlobalExceptionHandler.java
package com.example.demo.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException; // For @Valid errors
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice // This annotation makes it applicable to all controllers
@RestController // So it can return JSON responses
public class GlobalExceptionHandler {

    // --- Handlers for your custom exceptions (recommended approach) ---

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Object> handleDuplicateEmailException(
            DuplicateEmailException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value()); // HTTP 409 Conflict
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("field", "email"); // Specific field for frontend to highlight
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<Object> handleDuplicateUsernameException(
            DuplicateUsernameException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("field", "username");
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DuplicateContactInfoException.class)
    public ResponseEntity<Object> handleDuplicateContactInfoException(
            DuplicateContactInfoException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("field", "contactInfo");
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // --- Handler for @Valid DTO validation errors (400 Bad Request) ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Validation failed for request body.");
        body.put("errors", errors); // Contains specific field errors
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // --- Fallback for database-level unique constraint violations (409 Conflict) ---
    // This catches errors if the database constraint fires before your service-level check
    // or for other unique constraints.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");

        String errorMessage = "A data integrity violation occurred.";
        if (ex.getMostSpecificCause() != null) {
            String rootCauseMessage = ex.getMostSpecificCause().getMessage();
            if (rootCauseMessage.contains("Duplicate entry") || rootCauseMessage.contains("unique constraint")) {
                if (rootCauseMessage.contains("email")) { // Customize based on your column names
                    errorMessage = "The email address provided is already in use.";
                } else if (rootCauseMessage.contains("username")) {
                    errorMessage = "The username provided is already in use.";
                } else if (rootCauseMessage.contains("contactInfo")) {
                    errorMessage = "The contact information provided is already in use.";
                } else {
                    errorMessage = "A duplicate entry was detected.";
                }
            }
        }
        body.put("message", errorMessage);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // --- Generic handler for all other unhandled exceptions (500 Internal Server Error) ---
    // This should be the last exception handler to catch anything else.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(
            Exception ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred. Please try again later.");
        body.put("path", request.getDescription(false).replace("uri=", ""));

        // IMPORTANT: Log the full stack trace for serious debugging on the server side
        ex.printStackTrace();

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}