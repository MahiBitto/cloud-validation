package com.crypto.keymaker.exception;

import com.crypto.keymaker.dto.base.KeymakerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.UUID;

@RestControllerAdvice
public class CentralizedKeymakerExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<KeymakerResponse> handleGenericSystemOutages(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(KeymakerResponse.failure(UUID.randomUUID().toString(), ex.getMessage()));
    }
}
