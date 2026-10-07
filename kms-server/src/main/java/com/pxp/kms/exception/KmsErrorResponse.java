package com.pxp.kms.exception;


public class KmsErrorResponse {
    private final String errorCode;
    private final String message;
    private final long timestamp;

    public KmsErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
