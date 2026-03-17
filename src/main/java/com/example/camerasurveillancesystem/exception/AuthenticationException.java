package com.example.camerasurveillancesystem.exception;

/**
 * Exception thrown for authentication and authorization errors
 */
public class AuthenticationException extends AppException {
    
    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public AuthenticationException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
    
    public AuthenticationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
