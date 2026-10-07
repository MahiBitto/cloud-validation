package com.crypto.keymaker.repository;

import java.util.Optional;

import org.springframework.data.repository.query.Param;

import com.crypto.keymaker.entity.KekInfo;
import com.google.cloud.spring.data.spanner.repository.SpannerRepository;
import com.google.cloud.spring.data.spanner.repository.query.Query;

//@Repository 
public interface KekInfoRepository extends SpannerRepository<KekInfo, String> {
    /* @Query("SELECT k FROM KekInfo k WHERE k.kekType = :type AND k.isActive = true AND k.dekCnt < 100 ORDER BY k.creatTs ASC LIMIT 1")
    Optional<KekInfo> findAvailableKek(@Param("type") String type);

    @Query("SELECT COUNT(k) FROM KekInfo k WHERE k.kekType = :type AND k.isActive = true AND k.dekCnt < 100")
    long countAvailablePoolKeks(@Param("type") String type); */

    // 1. Explicit Spanner SQL lookup to pull the oldest active KEK under the 100 DEK threshold
    @Query("SELECT * FROM kek_info WHERE kek_type = @type AND is_active = true AND dek_cnt < 100 ORDER BY creat_ts ASC LIMIT 1")
    Optional<KekInfo> findAvailableKek(@Param("type") String type);

    // 2. Explicit Spanner SQL counting query for background pool checking
    @Query("SELECT COUNT(1) FROM kek_info WHERE kek_type = @type AND is_active = true AND dek_cnt < 100")
    long countAvailablePoolKeks(@Param("type") String type);
}
