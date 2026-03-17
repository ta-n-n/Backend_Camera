package com.example.camerasurveillancesystem.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all custom exceptions in the application
 */
@Getter
public class AppException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final String errorCodeString;
    
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.errorCodeString = errorCode.getCode();
    }
    
    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.errorCodeString = errorCode.getCode();
    }
    
    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.errorCodeString = errorCode.getCode();
    }
    
    public AppException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.errorCodeString = errorCode.getCode();
    }
}
