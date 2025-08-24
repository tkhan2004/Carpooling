package org.example.carpooling.Payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Standard API response format for all controllers
 * @param <T> Type of data being returned
 */
@Data
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean isSuccess;
    private String message;
    private int statusCode;
    private T data;

    /**
     * Constructor with all fields
     * @param isSuccess Whether the request was successful
     * @param message Response message
     * @param statusCode HTTP status code
     * @param data Response data
     */
    public ApiResponse(boolean isSuccess, String message, int statusCode, T data) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.statusCode = statusCode;
        this.data = data;
    }

    /**
     * Constructor without data
     * @param isSuccess Whether the request was successful
     * @param message Response message
     * @param statusCode HTTP status code
     */
    public ApiResponse(boolean isSuccess, String message, int statusCode) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.statusCode = statusCode;
    }

    /**
     * Constructor with HttpStatus enum
     * @param isSuccess Whether the request was successful
     * @param message Response message
     * @param status HTTP status
     * @param data Response data
     */
    public ApiResponse(boolean isSuccess, String message, HttpStatus status, T data) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.statusCode = status.value();
        this.data = data;
    }

    /**
     * Constructor with HttpStatus enum without data
     * @param isSuccess Whether the request was successful
     * @param message Response message
     * @param status HTTP status
     */
    public ApiResponse(boolean isSuccess, String message, HttpStatus status) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.statusCode = status.value();
    }

    /**
     * Legacy constructor for backward compatibility
     * @param isSuccess Whether the request was successful
     * @param message Response message
     * @param data Response data
     */
    public ApiResponse(boolean isSuccess, String message, T data) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.statusCode = isSuccess ? HttpStatus.OK.value() : HttpStatus.BAD_REQUEST.value();
        this.data = data;
    }

    /**
     * Legacy constructor for backward compatibility
     * @param isSuccess Whether the request was successful
     * @param message Response message
     */
    public ApiResponse(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
        this.statusCode = isSuccess ? HttpStatus.OK.value() : HttpStatus.BAD_REQUEST.value();
    }
}
