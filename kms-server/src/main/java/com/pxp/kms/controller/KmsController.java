package com.pxp.kms.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pxp.kms.service.KeyManagementService;

@RestController
@RequestMapping("/v1/keys")
public class KmsController {

    private final KeyManagementService kmsService;

    public KmsController(KeyManagementService kmsService) {
        this.kmsService = kmsService;
    }


    /*
    curl -X POST http://localhost:8074/v1/keys/generate \
     -H "Content-Type: application/json" \
     -d '{"keyIdentifier": "payment-service-key-v1", "cryptoType": "AES"}'
    */
    @PostMapping("/generate")
    public Map<String, String> generateKey(@RequestBody Map<String, String> request) throws Exception {
        kmsService.generateAndStoreKey(request.get("keyIdentifier"), request.get("cryptoType"));
        return Map.of("status", "SUCCESS");
    }

    @PostMapping("/retrieve")
    public Map<String, String> retrieveKey(@RequestBody Map<String, String> request) throws Exception {
        // Enforce X-API-KEY header validation here in a standard interceptor/filter
        String plaintextBase64 = kmsService.retrievePlaintextKey(request.get("keyIdentifier"));
        return Map.of("keyMaterial", plaintextBase64);
    }
}