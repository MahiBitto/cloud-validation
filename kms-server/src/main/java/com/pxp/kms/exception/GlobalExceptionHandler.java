package com.pxp.kms.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Catch specific managed business failures
    @ExceptionHandler(KmsServiceException.class)
    public ResponseEntity<KmsErrorResponse> handleKmsException(KmsServiceException ex) {
        // Log locally internally for debugging, omitting details from the exposed response
        logger.error("KMS Business Exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        
        KmsErrorResponse error = new KmsErrorResponse(ex.getErrorCode(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Catch fallback raw system/crypto/DB errors securely
    @ExceptionHandler(Exception.class)
    public ResponseEntity<KmsErrorResponse> handleGenericException(Exception ex) {
        // 🔒 SAFE LOGGING: Full stack trace goes strictly to the server console/file
        logger.error("CRITICAL CRYPTO/SYSTEM ERROR OCCURRED - HIDING FROM CLIENT", ex);

        // Client only gets a sanitized generic payload
        KmsErrorResponse error = new KmsErrorResponse(
                "KMS_INTERNAL_CRYPTOGRAPHIC_ERROR", 
                "An internal cryptographic processing error occurred. Check server logs."
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
