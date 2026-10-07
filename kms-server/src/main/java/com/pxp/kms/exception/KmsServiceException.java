package com.pxp.kms.exception;

public class KmsServiceException extends RuntimeException {
    private final String errorCode;

    public KmsServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public KmsServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
