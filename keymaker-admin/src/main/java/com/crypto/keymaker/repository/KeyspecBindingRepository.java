package com.crypto.keymaker.repository;

import com.crypto.keymaker.entity.KeyspecBinding;
import com.google.cloud.spring.data.spanner.repository.SpannerRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//@Repository 
public interface KeyspecBindingRepository extends SpannerRepository<KeyspecBinding, String> {
    Optional<KeyspecBinding> findByKeyspecInfoIdAndUserInfoId(String keyspecInfoId, String userInfoId);
}
