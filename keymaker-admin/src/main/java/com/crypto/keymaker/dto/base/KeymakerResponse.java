package com.crypto.keymaker.dto.base;

import java.time.Instant;

public record KeymakerResponse(
    String correlationId,
    String status,
    String message,
    String resourceId,
    Instant timestamp
) {
    public static KeymakerResponse success(String correlationId, String msg, String resId) {
        return new KeymakerResponse(correlationId, "SUCCESS", msg, resId, Instant.now());
    }
    public static KeymakerResponse failure(String correlationId, String msg) {
        return new KeymakerResponse(correlationId, "FAILED", msg, null, Instant.now());
    }
}
