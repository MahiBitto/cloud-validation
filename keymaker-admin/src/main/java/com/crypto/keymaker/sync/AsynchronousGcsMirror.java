package com.crypto.keymaker.sync;

import com.crypto.keymaker.dto.IdentityBind;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

@Component
public class AsynchronousGcsMirror {

    private static final Logger log = LoggerFactory.getLogger(AsynchronousGcsMirror.class);
    private final Storage storageClient;

    public AsynchronousGcsMirror(Storage storageClient) {
        this.storageClient = storageClient;
    }

    @Async("gcsExecutorPool")
    public void writeOnboardMetadataToGcs(String safename, String keyname, String type, String scope, String correlationId) {
        try {
            log.debug("[Async Outbox] Starting background metadata sync to GCS for safename: {}", safename);
            writeBlob(String.format("keymaker/%s/keyring.properties", safename), "safename=" + safename);
            writeBlob(String.format("keymaker/%s/%s/key.properties", safename, keyname), 
                      String.format("keyname=%s\ntype=%s\nscope=%s", keyname, type, scope));
            log.info("[Async Outbox Complete] Successfully mirrored onboarding keyspec files to GCS for ID: {}", correlationId);
        } catch (Exception e) {
            log.error("CRITICAL ALARM: GCS Onboard Sync Fail for tracking ID: {}! Reason: {}", correlationId, e.getMessage());
        }
    }

    @Async("gcsExecutorPool")
    public void writeBindingMutationToGcs(String safename, String keyname, IdentityBind identity, boolean isBind, String correlationId) {
        try {
            String folder = isBind ? "bind" : "unbind";
            String path = String.format("keymaker/%s/%s/%s/%s/%s.properties", safename, keyname, folder, identity.username(), folder);
            String text = String.format("username=%s\ntype=%s\nrole=%s\nplatform=%s", identity.username(), identity.user_type(), identity.user_role(), identity.user_platform());
            
            writeBlob(path, text);
            log.info("[Async Outbox Complete] Successfully mirrored binding update to GCS directory path for ID: {}", correlationId);
        } catch (Exception e) {
            log.error("CRITICAL ALARM: GCS Binding Sync Fail for tracking ID: {}! Reason: {}", correlationId, e.getMessage());
        }
    }

    private void writeBlob(String path, String data) {
        BlobId blobId = BlobId.of("pxp-bdp-keymaker-vault", path);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
        storageClient.create(blobInfo, data.getBytes(StandardCharsets.UTF_8));
    }
}
