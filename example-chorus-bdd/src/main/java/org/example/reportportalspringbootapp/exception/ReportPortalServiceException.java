package org.example.reportportalspringbootapp.exception;

/**
 * Custom exception for ReportPortal service operations
 */
public class ReportPortalServiceException extends RuntimeException {
    
    public ReportPortalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
