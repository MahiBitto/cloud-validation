package com.crypto.keymaker.service;

import com.crypto.keymaker.dao.KeyProfileDao;
import com.crypto.keymaker.dao.KeyspecBindingDao;
import com.crypto.keymaker.dto.BindRequest;
import com.crypto.keymaker.entity.*;
import com.crypto.keymaker.sync.AsynchronousGcsMirror;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class BindingService {
    private final KeyProfileDao profileDao;
    private final KeyspecBindingDao bindingDao;
    private final AsynchronousGcsMirror gcsMirror;
    private final Tracer tracer;

    public BindingService(KeyProfileDao profileDao, KeyspecBindingDao bindingDao, AsynchronousGcsMirror gcsMirror, Tracer tracer) {
        this.profileDao = profileDao;
        this.bindingDao = bindingDao;
        this.gcsMirror = gcsMirror;
        this.tracer = tracer;
    }

    @Transactional
    public void executeIdentityMutation(BindRequest request, boolean isBind, String correlationId) {
        Span span = tracer.spanBuilder("Keymaker.IdentityMutation").startSpan();
        try (Scope scope = span.makeCurrent()) {
            KeyspecInfo spec = profileDao.findActiveProfile(request.safename(), request.keyname())
                    .orElseThrow(() -> new NoSuchElementException("Target keyspec structure profile does not exist."));

            if (isBind) {
                UserInfo user = bindingDao.findExistingUser(request.identity().username(), request.identity().user_platform())
                        .orElseGet(() -> {
                            UserInfo u = new UserInfo();
                            u.setUserInfoId(UUID.randomUUID().toString());
                            u.setUsername(request.identity().username());
                            u.setUserType(request.identity().user_type());
                            u.setUserRole(request.identity().user_role());
                            u.setUserPlatform(request.identity().user_platform());
                            u.setActive(true);
                            u.setCreatTs(Instant.now());
                            return bindingDao.saveUserRecord(u);
                        });

                KeyspecBinding b = bindingDao.findLink(spec.getKeyspecInfoId(), user.getUserInfoId())
                        .orElseGet(() -> {
                            KeyspecBinding newB = new KeyspecBinding();
                            newB.setKeyspecBindingId(UUID.randomUUID().toString());
                            newB.setKeyspecInfoId(spec.getKeyspecInfoId());
                            newB.setUserInfoId(user.getUserInfoId());
                            return newB;
                        });
                b.setActive(true);
                bindingDao.saveBindingLink(b);
            } else {
                UserInfo user = bindingDao.findExistingUser(request.identity().username(), request.identity().user_platform())
                        .orElseThrow(() -> new NoSuchElementException("Identity not found."));
                bindingDao.findLink(spec.getKeyspecInfoId(), user.getUserInfoId()).ifPresent(b -> {
                    b.setActive(false);
                    bindingDao.saveBindingLink(b);
                });
            }
            gcsMirror.writeBindingMutationToGcs(request.safename(), request.keyname(), request.identity(), isBind, correlationId);
        } finally {
            span.end();
        }
    }
}
