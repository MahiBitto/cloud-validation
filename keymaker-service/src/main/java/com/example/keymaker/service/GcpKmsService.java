package com.example.keymaker.service;

import com.google.cloud.kms.v1.CryptoKeyName;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.cloud.kms.v1.KeyManagementServiceSettings;
import com.google.cloud.kms.v1.DecryptRequest;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GcpKmsService {

    private final KeyManagementServiceClient kmsClient;

    public GcpKmsService() throws IOException {
        this.kmsClient =
                KeyManagementServiceClient.create(
                        KeyManagementServiceSettings.newBuilder()
                                .build()
                );
    }

    public byte[] decrypt(
            String projectId,
            String location,
            String keyRing,
            String cryptoKey,
            byte[] ciphertext) {

        CryptoKeyName keyName =
                CryptoKeyName.of(
                        projectId,
                        location,
                        keyRing,
                        cryptoKey
                );

        DecryptRequest request =
                DecryptRequest.newBuilder()
                        .setName(keyName.toString())
                        .setCiphertext(
                                ByteString.copyFrom(ciphertext)
                        )
                        .build();

        return kmsClient.decrypt(request)
                .getPlaintext()
                .toByteArray();
    }
}
