package com.example.camerasurveillancesystem.exception;

/**
 * Exception thrown when a resource already exists (duplicate)
 */
public class DuplicateResourceException extends AppException {
    
    public DuplicateResourceException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public DuplicateResourceException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
    
    public DuplicateResourceException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
