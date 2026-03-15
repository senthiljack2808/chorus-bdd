package org.example.reportportalspringbootapp.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.reportportalspringbootapp.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", errors);

        ApiResponse<Map<String, String>> response = ApiResponse.error(
            "Validation failed",
            "Please check the provided data"
        );
        response.setData(errors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingParams(
            MissingServletRequestParameterException ex) {

        String message = String.format("Missing required parameter: %s", ex.getParameterName());
        log.warn(message);

        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Missing parameter", message));
    }

    /**
     * Handle type mismatch errors
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String message = String.format("Invalid value for parameter '%s': %s",
                ex.getName(), ex.getValue());
        log.warn(message);

        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid parameter type", message));
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex) {

        log.warn("Illegal argument: {}", ex.getMessage());
        
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid request", ex.getMessage()));
    }

    /**
     * Handle ReportPortal service exceptions
     */
    @ExceptionHandler(ReportPortalServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleReportPortalServiceException(
            ReportPortalServiceException ex) {

        log.error("ReportPortal service error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Service error", ex.getMessage()));
    }

    /**
     * Handle database service exceptions
     */
    @ExceptionHandler(DatabaseServiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDatabaseServiceException(
            DatabaseServiceException ex) {

        log.error("Database service error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Database error", ex.getMessage()));
    }

    /**
     * Handle all other runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex) {

        log.error("Unexpected runtime error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error",
                        "An unexpected error occurred. Please try again later."));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex) {

        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", 
                        "An unexpected error occurred. Please contact support."));
    }
}
