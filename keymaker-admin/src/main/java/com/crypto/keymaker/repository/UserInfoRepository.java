package com.crypto.keymaker.repository;

import com.crypto.keymaker.entity.UserInfo;
import com.google.cloud.spring.data.spanner.repository.SpannerRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//@Repository 
public interface UserInfoRepository extends SpannerRepository<UserInfo, String> {
    Optional<UserInfo> findByUsernameAndUserPlatform(String username, String platform);
}
