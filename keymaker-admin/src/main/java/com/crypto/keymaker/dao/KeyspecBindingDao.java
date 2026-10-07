package com.crypto.keymaker.dao;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.crypto.keymaker.entity.KeyspecBinding;
import com.crypto.keymaker.entity.UserInfo;
import com.crypto.keymaker.repository.KeyspecBindingRepository;
import com.crypto.keymaker.repository.UserInfoRepository;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;

@Component
public class KeyspecBindingDao {
    private final UserInfoRepository userRepo;
    private final KeyspecBindingRepository bindingRepo;
    private final Tracer tracer;

    public KeyspecBindingDao(UserInfoRepository userRepo, KeyspecBindingRepository bindingRepo, Tracer tracer) {
        this.userRepo = userRepo;
        this.bindingRepo = bindingRepo;
        this.tracer = tracer;
    }

    public Optional<UserInfo> findExistingUser(String username, String platform) {
        Span span = tracer.spanBuilder("SpannerDao.FindExistingUser").startSpan();
        try { return userRepo.findByUsernameAndUserPlatform(username, platform); } 
        finally { span.end(); }
    }

    public UserInfo saveUserRecord(UserInfo user) {
        Span span = tracer.spanBuilder("SpannerDao.SaveUserRecord").startSpan();
        try { return userRepo.save(user); } 
        finally { span.end(); }
    }

    public Optional<KeyspecBinding> findLink(String keyspecId, String userId) {
        Span span = tracer.spanBuilder("SpannerDao.FindLink").startSpan();
        try { return bindingRepo.findByKeyspecInfoIdAndUserInfoId(keyspecId, userId); } 
        finally { span.end(); }
    }

    public void saveBindingLink(KeyspecBinding binding) {
        Span span = tracer.spanBuilder("SpannerDao.SaveBindingLink").startSpan();
        try { bindingRepo.save(binding); } 
        finally { span.end(); }
    }
}
