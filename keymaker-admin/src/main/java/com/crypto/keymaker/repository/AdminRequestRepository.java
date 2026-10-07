package com.crypto.keymaker.repository;

import com.crypto.keymaker.entity.AdminRequest;
import com.google.cloud.spring.data.spanner.repository.SpannerRepository;

public interface AdminRequestRepository extends SpannerRepository<AdminRequest, String> {}
