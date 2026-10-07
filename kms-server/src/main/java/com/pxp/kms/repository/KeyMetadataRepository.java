package com.pxp.kms.repository;

import com.pxp.kms.model.KeyMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeyMetadataRepository extends JpaRepository<KeyMetadata, String> {}
