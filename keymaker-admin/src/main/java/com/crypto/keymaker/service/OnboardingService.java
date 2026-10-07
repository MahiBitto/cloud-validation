package com.crypto.keymaker.service;

import com.crypto.keymaker.dao.*;
import com.crypto.keymaker.dto.OnboardRequest;
import com.crypto.keymaker.entity.*;
import com.crypto.keymaker.sync.AsynchronousGcsMirror;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service
public class OnboardingService {
    private final KeyProfileDao profileDao;
    private final KekPoolDao poolDao;
    private final KeyMaterialDao materialDao;
    private final AsynchronousGcsMirror gcsMirror;
    private final ObjectMapper mapper;
    private final Tracer tracer;

    @Value("${keymaker.region}") private String currentRegion;

    public OnboardingService(KeyProfileDao profileDao, KekPoolDao poolDao, KeyMaterialDao materialDao,
                             AsynchronousGcsMirror gcsMirror, ObjectMapper mapper, Tracer tracer) {
        this.profileDao = profileDao;
        this.poolDao = poolDao;
        this.materialDao = materialDao;
        this.gcsMirror = gcsMirror;
        this.mapper = mapper;
        this.tracer = tracer;
    }

    @Transactional
    public String executeOnboard(OnboardRequest request, String correlationId) throws Exception {
        Span span = tracer.spanBuilder("Keymaker.ExecuteOnboard").startSpan();
        try (Scope scope = span.makeCurrent()) {
            Optional<KeyspecInfo> match = profileDao.findActiveProfile(request.safename(), request.keyname());
            if (match.isPresent()) return match.get().getKeyspecInfoId();

            KekInfo kek = poolDao.findAvailableKekForWrapping(request.key_type())
                    .orElseThrow(() -> new IllegalStateException("KEK Pool depleted. Schedulers lagging."));

            byte[] rawDek = (request.transitGlobalDekRaw() != null) ? request.transitGlobalDekRaw() : generateRawDek();

            KeyspecInfo keyspec = new KeyspecInfo();
            keyspec.setKeyspecInfoId(UUID.randomUUID().toString());
            keyspec.setSafename(request.safename());
            keyspec.setKeyname(request.keyname());
            keyspec.setKeyType(request.key_type());
            keyspec.setRegionalOrGlobal(request.regional_or_global());
            keyspec.setKeyRegion(currentRegion);
            keyspec.setActive(true);
            keyspec.setCreatTs(Instant.now());
            profileDao.saveProfile(keyspec);

            KeyMaterialInfo material = new KeyMaterialInfo();
            material.setKeyMaterialInfoId(UUID.randomUUID().toString());
            material.setKeyspecInfoId(keyspec.getKeyspecInfoId());
            material.setKekInfoId(kek.getKekInfoId());
            material.setKeyMaterialJsonBytes(mapper.writeValueAsBytes(Map.of("gcp", Base64.getEncoder().encodeToString(rawDek))));
            material.setActive(true);
            material.setCreatTs(Instant.now());
            materialDao.persistCryptographicMaterial(material);

            kek.setDekCnt(kek.getDekCnt() + 1);
            poolDao.saveKek(kek);

            gcsMirror.writeOnboardMetadataToGcs(request.safename(), request.keyname(), request.key_type(), request.regional_or_global(), correlationId);
            return keyspec.getKeyspecInfoId();
        } finally {
            span.end();
        }
    }

    private byte[] generateRawDek() throws Exception {
        javax.crypto.KeyGenerator kg = javax.crypto.KeyGenerator.getInstance("AES");
        kg.init(256);
        return kg.generateKey().getEncoded();
    }
}
