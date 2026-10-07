package com.crypto.keymaker.repository;

import java.time.Instant;
import java.util.Optional;

import com.crypto.keymaker.entity.KeyspecInfo;
import com.google.cloud.spring.data.spanner.repository.SpannerRepository;

//@Repository 
public interface KeyspecInfoRepository extends SpannerRepository<KeyspecInfo, String> {
    Optional<KeyspecInfo> findBySafenameAndKeyname(String safename, String keyname);
    
    /*@Query("SELECT COUNT(k) FROM KeyspecInfo k WHERE k.creatTs >= :startTime")
    long countGlobalKeysCreatedInWindow(@Param("startTime") Instant startTime);*/

    // Explicit Spanner native SQL statement allows custom method names
    //@Query("SELECT COUNT(1) FROM keyspec_info WHERE creat_ts >= @startTime")
    //long countGlobalKeysCreatedInWindow(@Param("startTime") Instant startTime);
    long countByCreatTsGreaterThanEqual(Instant startTime);
}
