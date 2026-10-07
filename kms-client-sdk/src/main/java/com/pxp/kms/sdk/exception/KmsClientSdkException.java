package com.pxp.kms.sdk.exception;

public class KmsClientSdkException extends Exception {
    private final String errorCode;

    public KmsClientSdkException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
