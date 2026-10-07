package com.pxp.kms.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

@Service
public class MasterKeyService {

    static { Security.addProvider(new BouncyCastleProvider()); }

    // 🌟 Update extension to .bks for clarity
    private static final String KEYSTORE_PATH = "./master-keystore.bks"; 
    private static final String ALIAS = "kms-master-kek";
    private static final char[] PASSWORD = "SuperSecurePassword123!".toCharArray(); 

    private SecretKey masterKey;

    public MasterKeyService() throws Exception {
        loadOrInitializeMasterKey();
    }

    private void loadOrInitializeMasterKey() throws Exception {
        // 🌟 FIX: Use "BKS" (Bouncy Castle KeyStore) which fully supports standalone secret keys
        KeyStore keyStore = KeyStore.getInstance("BKS", "BC");
        File file = new File(KEYSTORE_PATH);

        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                keyStore.load(fis, PASSWORD);
                this.masterKey = (SecretKey) keyStore.getKey(ALIAS, PASSWORD);
            }
        } else {
            // Create a brand new BKS keystore structure
            keyStore.load(null, PASSWORD);
            
            // Generate the master AES KEK
            KeyGenerator keyGen = KeyGenerator.getInstance("AES", "BC");
            keyGen.init(256, new SecureRandom());
            this.masterKey = keyGen.generateKey();

            // 🌟 Wrap it cleanly in a SecretKeyEntry
            KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(masterKey);
            KeyStore.ProtectionParameter param = new KeyStore.PasswordProtection(PASSWORD);
            keyStore.setEntry(ALIAS, entry, param);

            // Save to disk
            try (FileOutputStream fos = new FileOutputStream(file)) {
                keyStore.store(fos, PASSWORD);
            }
        }
    }

    public SecretKey getMasterKey() {
        return this.masterKey;
    }
}