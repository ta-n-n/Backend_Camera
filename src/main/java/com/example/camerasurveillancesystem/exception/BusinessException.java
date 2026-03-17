package com.example.camerasurveillancesystem.exception;

/**
 * Exception thrown for invalid business operations
 */
public class BusinessException extends AppException {
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
    
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
