package org.example.reportportalspringbootapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized API response wrapper
 *
 * @param <T> The type of data being returned
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;
    private LocalDateTime timestamp;

    /**
     * Creates a successful response with data and custom message
     *
     * @param data    The response data
     * @param message Custom success message
     * @param <T>     The type of data
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now());
    }

    /**
     * Creates an error response with custom message
     *
     * @param message Custom error message
     * @param error   Detailed error information
     * @param <T>     The type of data
     * @return ApiResponse with error status
     */
    public static <T> ApiResponse<T> error(String message, String error) {
        return new ApiResponse<>(false, message, null, error, LocalDateTime.now());
    }
}
