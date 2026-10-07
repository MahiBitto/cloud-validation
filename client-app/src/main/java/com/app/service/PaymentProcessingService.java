package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pxp.kms.KmsClient;
import com.pxp.kms.sdk.exception.KmsClientSdkException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentProcessingService {

    @Autowired
    private KmsClient kmsClient;

    public void processCreditCard(String rawCardNum) {
        try {
            // Sub-millisecond execution if cached locally by the SDK!
            //String cipherText = kmsClient.encryptAES("fin-service-card-key", rawCardNum);
            String cipherText = kmsClient.encryptAES("payment-service-key-v1", rawCardNum);

            System.out.println("Secure encrypted payload to save in DB: " + cipherText);
        } catch (KmsClientSdkException e) {
            // Highly readable domain context!
            System.err.println("KMS Failed with Custom Code: " + e.getErrorCode());
            System.err.print(", Failure Reason: " + e.getMessage() +" \n\n");
            
            // Decoupled raw crypto dump - No low-level Bouncy Castle security components are leaked!
        } catch (Exception e) {
            log.error("Crypto operation failed", e);
        }
    }
}