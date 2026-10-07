package com.crypto.keymaker.scheduler;

import com.crypto.keymaker.dao.KekPoolDao;
import com.crypto.keymaker.dao.KeyProfileDao;
import com.crypto.keymaker.entity.KekInfo;
import com.crypto.keymaker.integration.GcpKmsBulkImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class OperationalGovernanceSchedulers {

    private static final Logger log = LoggerFactory.getLogger(OperationalGovernanceSchedulers.class);

    private final KekPoolDao poolDao;
    private final KeyProfileDao profileDao;
    private final GcpKmsBulkImporter gcpKmsImporter;

    // Thread-safe atomic flag to manage window throttling states
    private final AtomicBoolean slidingWindowBreachedFlag = new AtomicBoolean(false);

    private static final int FLOOR_THRESHOLD = 5;
    private static final int BULK_PROVISION_SIZE = 10;

    public OperationalGovernanceSchedulers(KekPoolDao poolDao, KeyProfileDao profileDao, GcpKmsBulkImporter gcpKmsImporter) {
        this.poolDao = poolDao;
        this.profileDao = profileDao;
        this.gcpKmsImporter = gcpKmsImporter;
    }

    /**
     * FIX: Public getter exposed directly to Controllers and Listeners
     */
    public boolean isPipelineThrottled() {
        return slidingWindowBreachedFlag.get();
    }

    @Scheduled(fixedRate = 300000) // Runs every 5 minutes
    public void balanceKekPoolCushions() {
        String[] types = {"batch", "online_deterministic", "online_nondeterministic"};
        
        for (String type : types) {
            try {
                long currentAvailablePool = poolDao.getActivePoolCushionSize(type);
                
                if (currentAvailablePool < FLOOR_THRESHOLD) {
                    log.info("[Scheduler] Pool size for '{}' is low ({}). Pre-allocating {} KEKs...", type, currentAvailablePool, BULK_PROVISION_SIZE);
                    
                    for (int i = 0; i < BULK_PROVISION_SIZE; i++) {
                        try {
                            String randomIdentifier = UUID.randomUUID().toString().substring(0, 10);
                            byte[] generatedOnPremKekBytes = generateOnPremKekBytes(type); 
                            log.info("calling GCP KMS to create KEK/ import material into GCP KMS System");
                            String actualGcpKeyUri = gcpKmsImporter.importOnPremKekToGcpKms(randomIdentifier, generatedOnPremKekBytes, type);

                            KekInfo k = new KekInfo();
                            k.setKekInfoId(UUID.randomUUID().toString());
                            k.setOnpremKekIdentifier("onprem-bulk-id-" + randomIdentifier);
                            k.setCloudKekIdentifier(actualGcpKeyUri);
                            k.setKekType(type);
                            k.setActive(true);
                            k.setDekCnt(0);
                            k.setCreatTs(Instant.now());
                            k.setLstUpdtTs(Instant.now());
                            
                            poolDao.saveKek(k);
                            
                        } catch (Exception keyEx) {
                            log.error("[Scheduler Error] Failed to provision KEK index {}: {}", i, keyEx.getMessage());
                        }
                    }
                }
            } catch (Exception poolEx) {
                log.error("[Scheduler Error] Failed to evaluate pool cushions for type '{}': {}", type, poolEx.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 60000) // Runs every 1 minute
    public void monitorSlidingOnboardWindows() {
        try {
            Instant trailingWindowLimit = Instant.now().minus(12, ChronoUnit.HOURS);
            long count = profileDao.getCountCreatedWithinWindow(trailingWindowLimit);

            if (count >= 500) {
                if (slidingWindowBreachedFlag.compareAndSet(false, true)) {
                    log.warn("CRITICAL: 12-Hour onboarding threshold breached ({} entries). Throttling onboarding pipelines.", count);
                }
            } else {
                if (slidingWindowBreachedFlag.compareAndSet(true, false)) {
                    log.info("Governance window normalization restored. Re-enabling processing streams.");
                }
            }
        } catch (Exception e) {
            log.error("[Scheduler Error] Window monitor evaluation crash: {}", e.getMessage());
        }
    }

    private byte[] generateOnPremKekBytes(String type) {
        byte[] keyBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(keyBytes);
        return keyBytes;
    }
}
