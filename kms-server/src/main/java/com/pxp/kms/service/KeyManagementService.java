package com.pxp.kms.service;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;

import org.springframework.stereotype.Service;

import com.pxp.kms.exception.ErrorConstants;
import com.pxp.kms.exception.KmsServiceException;
import com.pxp.kms.model.KeyMetadata;
import com.pxp.kms.repository.KeyMetadataRepository;

@Service
public class KeyManagementService {

    private final KeyMetadataRepository repository;
    private final MasterKeyService masterKeyService;

    public KeyManagementService(KeyMetadataRepository repository, MasterKeyService masterKeyService) {
        this.repository = repository;
        this.masterKeyService = masterKeyService;
    }

    public void generateAndStoreKey(String keyId, String cryptoType) throws Exception {
        // 1. Generate requested key material (e.g., raw AES-256 key)
        KeyGenerator keyGen = KeyGenerator.getInstance(cryptoType, "BC");
        keyGen.init(256, new SecureRandom());
        byte[] rawKeyBytes = keyGen.generateKey().getEncoded();

        // 2. Encrypt this raw key using the Master KEK (Envelope Encryption)
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, masterKeyService.getMasterKey(), new GCMParameterSpec(128, iv));
        byte[] encryptedBytes = cipher.doFinal(rawKeyBytes);

        // Bundle IV and Ciphertext together for database storage
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        // 3. Persist to DB
        KeyMetadata meta = new KeyMetadata();
        meta.setKeyIdentifier(keyId);
        meta.setCryptoType(cryptoType);
        meta.setEncryptedKeyMaterial(Base64.getEncoder().encodeToString(combined));
        repository.save(meta);

        // Memory cleanup immediately
        java.util.Arrays.fill(rawKeyBytes, (byte) 0);
    }

    public String retrievePlaintextKey(String keyId) throws Exception {
        KeyMetadata meta = repository.findById(keyId)
                .orElseThrow(() -> new KmsServiceException(ErrorConstants.KEY_IDENTIFIER_NOT_FOUND, "The requested key identifier does not exist."));

        byte[] combined = Base64.getDecoder().decode(meta.getEncryptedKeyMaterial());
        
        byte[] iv = new byte[12];
        byte[] ciphertext = new byte[combined.length - 12];
        System.arraycopy(combined, 0, iv, 0, 12);
        System.arraycopy(combined, 12, ciphertext, 0, ciphertext.length);

        // Decrypt the raw key using the Master KEK
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, masterKeyService.getMasterKey(), new GCMParameterSpec(128, iv));
        byte[] decryptedKeyBytes = cipher.doFinal(ciphertext);

        String base64Result = Base64.getEncoder().encodeToString(decryptedKeyBytes);
        
        // Memory cleanup
        java.util.Arrays.fill(decryptedKeyBytes, (byte) 0);
        return base64Result;
    }



    public String retrievePlaintextKeyAutoGenerateIfNotFound(String keyId) throws Exception {
        // 🌟 Fix: If the client asks for a key that doesn't exist, build it right now!
        if (!repository.existsById(keyId)) {
            System.out.println("Key [" + keyId + "] not found. Auto-generating on the fly...");
            generateAndStoreKey(keyId, "AES"); 
        }

        KeyMetadata meta = repository.findById(keyId)
                .orElseThrow(() -> new RuntimeException("Key unexpectedly missing"));

        byte[] combined = Base64.getDecoder().decode(meta.getEncryptedKeyMaterial());
        
        byte[] iv = new byte[12];
        byte[] ciphertext = new byte[combined.length - 12];
        System.arraycopy(combined, 0, iv, 0, 12);
        System.arraycopy(combined, 12, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, masterKeyService.getMasterKey(), new GCMParameterSpec(128, iv));
        byte[] decryptedKeyBytes = cipher.doFinal(ciphertext);

        String base64Result = Base64.getEncoder().encodeToString(decryptedKeyBytes);
        
        java.util.Arrays.fill(decryptedKeyBytes, (byte) 0);
        return base64Result;
    }
}