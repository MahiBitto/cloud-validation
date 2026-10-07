package com.crypto.keymaker.integration;

import com.google.cloud.kms.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

@Service
public class GcpKmsBulkImporter {

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${keymaker.region}")
    private String locationId; // e.g., "us-central1" or "asia-south1"

    private static final String KEY_RING_NAME = "keymaker-kek-ring";

    /**
     * Executes a secure cryptographic import of pre-generated on-premise KEK material into a targeted GCP KMS key ring.
     */
    public String importOnPremKekToGcpKms(String kekIdentifier, byte[] rawOnPremKekBytes, String keyType) throws Exception {
        
        try (KeyManagementServiceClient client = KeyManagementServiceClient.create()) {
            
            String keyRingPath = KeyRingName.format(projectId, locationId, KEY_RING_NAME);
            String cryptoKeyId = "kek-" + kekIdentifier;
            String cryptoKeyPath = CryptoKeyName.format(projectId, locationId, KEY_RING_NAME, cryptoKeyId);

            // Step 1: Create a targeted placeholder target key inside GCP KMS HSM if it doesn't exist
            CryptoKey cryptoKeyPlaceholder = CryptoKey.newBuilder()
                    .setPurpose(CryptoKey.CryptoKeyPurpose.ENCRYPT_DECRYPT)
                    .setVersionTemplate(CryptoKeyVersionTemplate.newBuilder()
                            .setProtectionLevel(ProtectionLevel.HSM) // Must be protected by physical Cloud HSM
                            .setAlgorithm(keyType.equalsIgnoreCase("batch") 
                                    ? CryptoKeyVersion.CryptoKeyVersionAlgorithm.RSA_DECRYPT_OAEP_4096_SHA256 
                                    : CryptoKeyVersion.CryptoKeyVersionAlgorithm.GOOGLE_SYMMETRIC_ENCRYPTION))
                    .build();

            try {
                client.createCryptoKey(keyRingPath, cryptoKeyId, cryptoKeyPlaceholder);
            } catch (com.google.api.gax.rpc.AlreadyExistsException e) {
                System.out.println("GCP KMS key footprint already configured. Injecting fresh new version block...");
            }

            // Step 2: Provision a temporary secure Import Job
            String importJobId = "job-" + UUID.randomUUID().toString().substring(0, 8);
            ImportJob importJobConfig = ImportJob.newBuilder()
                    .setImportMethod(ImportJob.ImportMethod.RSA_OAEP_3072_SHA1_AES_256) // Crypto envelope standard
                    .setProtectionLevel(ProtectionLevel.HSM)
                    .build();

            ImportJob createdJob = client.createImportJob(keyRingPath, importJobId, importJobConfig);

            // Wait for the Import Job to move to an ACTIVE state
            while (createdJob.getState() != ImportJob.ImportJobState.ACTIVE) {
                Thread.sleep(200);
                createdJob = client.getImportJob(createdJob.getName());
            }

            // Step 3: Extract and decode the temporary GCP Public Wrapping Key
            String pemPublicKey = createdJob.getPublicKey().getPem();
            pemPublicKey = pemPublicKey
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            
            byte[] decodedKeyBytes = Base64.getDecoder().decode(pemPublicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKeyBytes);
            PublicKey gcpTargetPublicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);

            // Step 4: Encrypt the on-prem KEK material locally using the GCP public key via RSA-OAEP
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                    "SHA-1", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.WRAP_MODE, gcpTargetPublicKey, oaepParams);
            
            byte[] encryptedKekPayload = cipher.doFinal(rawOnPremKekBytes);

            // Step 5: Securely upload the encrypted payload into GCP KMS
            ImportCryptoKeyVersionRequest importRequest = ImportCryptoKeyVersionRequest.newBuilder()
                    .setParent(cryptoKeyPath)
                    .setAlgorithm(keyType.equalsIgnoreCase("batch")
                            ? CryptoKeyVersion.CryptoKeyVersionAlgorithm.RSA_DECRYPT_OAEP_4096_SHA256
                            : CryptoKeyVersion.CryptoKeyVersionAlgorithm.GOOGLE_SYMMETRIC_ENCRYPTION)
                    .setImportJob(createdJob.getName())
                    .setWrappedKey(ByteString.copyFrom(encryptedKekPayload))
                    .build();

            CryptoKeyVersion importedVersion = client.importCryptoKeyVersion(importRequest);
            
            System.out.printf("Successfully imported KEK version to GCP KMS: %s%n", importedVersion.getName());
            return importedVersion.getName(); // Returns the unique cloud key resource path mapping identifier
        }
    }
}
