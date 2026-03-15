package org.example.reportportalspringbootapp.exception;

/**
 * Custom exception for database service operations
 */
public class DatabaseServiceException extends RuntimeException {


    public DatabaseServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
