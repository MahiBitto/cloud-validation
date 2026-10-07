package com.crypto.keymaker.repository;

import com.crypto.keymaker.entity.KeyMaterialInfo;
import com.google.cloud.spring.data.spanner.repository.SpannerRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//@Repository 
public interface KeyMaterialInfoRepository extends SpannerRepository<KeyMaterialInfo, String> {}
