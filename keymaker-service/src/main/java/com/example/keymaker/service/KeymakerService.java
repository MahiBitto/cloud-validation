package com.example.keymaker.service;

import com.example.keymaker.model.KeyMaterialRecord;
import com.example.keymaker.repository.KeyMaterialRepository;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Properties;

import com.example.keymaker.config.KeymakerGcpProperties;

@Service
public class KeymakerService {

    private final KeyMaterialRepository repository;

    private final GcpKmsService kmsService;

    private final KeymakerGcpProperties gcpProperties;
    
    public KeymakerService(
                KeyMaterialRepository repository,
                GcpKmsService kmsService,
                KeymakerGcpProperties gcpProperties) {

        this.repository = repository;
        this.kmsService = kmsService;
        this.gcpProperties = gcpProperties;
    }

    public KeyMaterialRecord findKeyMaterial(
            String safeName,
            String keyName,
            String keyRegion) {

        return repository.findActiveKeyMaterial(
                safeName,
                keyName,
                keyRegion
        );
    }


    public byte[] fetchAndDecrypt(
            String safeName,
            String keyName,
            String keyRegion) {

        KeyMaterialRecord record =
                repository.findActiveKeyMaterial(
                        safeName,
                        keyName,
                        keyRegion
                );

        if (record == null) {
            throw new IllegalArgumentException(
                    "Key material not found"
            );
        }

        try {
            byte[] propertiesBytes =
                    Base64.getDecoder().decode(
                            record.keyMaterialJsonB64()
                    );

            Properties properties = new Properties();

            properties.load(
                    new java.io.ByteArrayInputStream(
                            propertiesBytes
                    )
            );

            String wrappedDek =
                    properties.getProperty("gcpWrappedDEK");

            if (wrappedDek == null) {
                throw new IllegalStateException(
                        "gcpWrappedDEK not found"
                );
            }

            byte[] ciphertext =
                    Base64.getDecoder().decode(wrappedDek);

            byte[] decryptedDek =
                    kmsService.decrypt(
                            gcpProperties.projectId(),
                            record.keyRegion(),
                            record.cloudKeyringName(),
                            record.cloudKekIdentifier(),
                            ciphertext
                    );

            if (decryptedDek.length != 32) {
                throw new IllegalStateException(
                        "Unexpected DEK length: "
                                + decryptedDek.length
                );
            }

            return decryptedDek;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to decrypt Keymaker DEK",
                    e
            );
        }
    }
}
