package com.crypto.keymaker.repository;

import com.crypto.keymaker.entity.AdminResp;
import com.google.cloud.spring.data.spanner.repository.SpannerRepository;

public interface AdminRespRepository extends SpannerRepository<AdminResp, String> {}
