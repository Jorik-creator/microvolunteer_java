package com.microvolunteer.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * Handles exceptions and converts them to RFC 7807 Problem Detail responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle business logic exceptions
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        logger.warn("Business exception: {}", ex.getMessage());
        
        HttpStatus status = determineStatusFromErrorCode(ex.getErrorCode());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        
        problemDetail.setType(URI.create("/errors/" + ex.getErrorCode().toLowerCase()));
        problemDetail.setTitle(getTitle(ex.getErrorCode()));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        problemDetail.setProperty("timestamp", Instant.now());
        
        return ResponseEntity.status(status).body(problemDetail);
    }
    
    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logger.warn("Validation exception: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Validation failed for one or more fields");
        
        problemDetail.setType(URI.create("/errors/validation"));
        problemDetail.setTitle("Validation Error");
        problemDetail.setProperty("timestamp", Instant.now());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        problemDetail.setProperty("validationErrors", errors);
        
        return ResponseEntity.badRequest().body(problemDetail);
    }
    
    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        logger.warn("Access denied: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN, "Access denied to the requested resource");
        
        problemDetail.setType(URI.create("/errors/access-denied"));
        problemDetail.setTitle("Access Denied");
        problemDetail.setProperty("timestamp", Instant.now());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problemDetail);
    }
    
    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        
        problemDetail.setType(URI.create("/errors/illegal-argument"));
        problemDetail.setTitle("Invalid Argument");
        problemDetail.setProperty("timestamp", Instant.now());
        
        return ResponseEntity.badRequest().body(problemDetail);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, WebRequest request) {
        
        logger.error("Unexpected error occurred", ex);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        
        problemDetail.setType(URI.create("/errors/internal-server-error"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setProperty("timestamp", Instant.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }
    
    /**
     * Determine HTTP status based on error code
     */
    private HttpStatus determineStatusFromErrorCode(String errorCode) {
        return switch (errorCode) {
            case "USER_NOT_FOUND", "TASK_NOT_FOUND", "CATEGORY_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "USER_ALREADY_EXISTS", "CATEGORY_ALREADY_EXISTS", "ALREADY_PARTICIPATING", 
                 "INVALID_STATUS_TRANSITION", "CANNOT_PARTICIPATE_OWN_TASK", "TASK_NOT_OPEN" -> HttpStatus.CONFLICT;
            case "UNAUTHORIZED_ACCESS" -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
    
    /**
     * Get human-readable title for error code
     */
    private String getTitle(String errorCode) {
        return switch (errorCode) {
            case "USER_NOT_FOUND" -> "User Not Found";
            case "TASK_NOT_FOUND" -> "Task Not Found";
            case "CATEGORY_NOT_FOUND" -> "Category Not Found";
            case "USER_ALREADY_EXISTS" -> "User Already Exists";
            case "CATEGORY_ALREADY_EXISTS" -> "Category Already Exists";
            case "ALREADY_PARTICIPATING" -> "Already Participating";
            case "INVALID_STATUS_TRANSITION" -> "Invalid Status Transition";
            case "CANNOT_PARTICIPATE_OWN_TASK" -> "Cannot Participate in Own Task";
            case "TASK_NOT_OPEN" -> "Task Not Open";
            case "UNAUTHORIZED_ACCESS" -> "Unauthorized Access";
            default -> "Business Error";
        };
    }
}
