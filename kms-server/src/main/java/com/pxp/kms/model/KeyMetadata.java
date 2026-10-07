package com.pxp.kms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "key_store_metadata")
public class KeyMetadata {
    
    @Id
    private String keyIdentifier;
    private String cryptoType; // AES, RSA, PGP
    
    @Column(length = 2048)
    private String encryptedKeyMaterial; // The DEK encrypted by the Master Key
    
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
    public String getKeyIdentifier() { return keyIdentifier; }
    public void setKeyIdentifier(String id) { this.keyIdentifier = id; }
    public String getCryptoType() { return cryptoType; }
    public void setCryptoType(String type) { this.cryptoType = type; }
    public String getEncryptedKeyMaterial() { return encryptedKeyMaterial; }
    public void setEncryptedKeyMaterial(String material) { this.encryptedKeyMaterial = material; }
}
