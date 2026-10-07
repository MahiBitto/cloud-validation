package com.pxp.kms;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.pxp.kms.sdk.exception.KmsClientSdkException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KmsClient {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final String baseUrl;
    private final String apiKey;
    private final OkHttpClient httpClient;
    // Cache decrypted keys for 1 hour to prevent flooding the KMS server
    private final Cache<String, byte[]> keyCache;

    public KmsClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient();
        this.keyCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .maximumSize(1000)
                .build();
    }

    /**
     * Resolves a key from local cache or remote KMS Server
     */
    private byte[] getOrFetchKey(String keyIdentifier) throws KmsClientSdkException {
        try {
            return keyCache.get(keyIdentifier, id -> {
                Request request = new Request.Builder()
                        .url(baseUrl + "/v1/keys/retrieve")
                        .post(RequestBody.create("{\"keyIdentifier\":\"" + id + "\"}", MediaType.get("application/json")))
                        .addHeader("X-API-KEY", apiKey)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    String responseBody = response.body() != null ? response.body().string() : "";

                    if (!response.isSuccessful()) {
                        // Try to safely pull internal custom metadata fields
                        String errCode = extractJsonValue(responseBody, "errorCode");
                        String errMsg = extractJsonValue(responseBody, "message");

                        if (errCode.isEmpty()) {
                            errCode = "HTTP_ERROR_" + response.code();
                            errMsg = "Server returned raw unexpected error status.";
                        }
                        // Throw internal runtime wrapper to break Caffeine computation block
                        throw new RuntimeException(errCode + "||" + errMsg);
                    }

                    String base64Key = extractJsonValue(responseBody, "keyMaterial");
                    return Base64.getDecoder().decode(base64Key);
                } catch (Exception e) {
                    throw new RuntimeException("NETWORK_CONNECTION_FAILED||Cannot reach KMS Server instance.", e);
                }
            });
        } catch (RuntimeException ex) {
            // Unpack the wrapper exception cleanly back into an explicit SDK checked exception
            String[] parts = ex.getCause() != null ? ex.getCause().getMessage().split("\\|\\|") : ex.getMessage().split("\\|\\|");
            if (parts.length >= 2) {
                throw new KmsClientSdkException(parts[0], parts[1]);
            }
            throw new KmsClientSdkException("SDK_UNKNOWN_PROCESSING_FAULT", ex.getMessage());
        }
    }

    /**
     * Local AES-GCM Encryption executed inside Client App memory space
     */
    public String encryptAES(String keyIdentifier, String plaintext) throws Exception {
        byte[] rawKey = getOrFetchKey(keyIdentifier);
        
        byte[] iv = new byte[12]; // 12 bytes IV is standard for GCM
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        SecretKeySpec keySpec = new SecretKeySpec(rawKey, "AES");
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));

        // Combine IV and Ciphertext for easier storage by the client
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    private String extractJsonValue(String json, String key) {
        // Simple manual parsing to avoid forcing Jackson/Gson dependencies on the client
        return json.split("\"" + key + "\":\"")[1].split("\"")[0];
    }
}