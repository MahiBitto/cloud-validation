package com.crypto.keymaker.dto;

import com.crypto.keymaker.dto.base.BaseKeyRequest;

public record BindRequest(
    String safename,
    String keyname,
    IdentityBind identity
) implements BaseKeyRequest {
    @Override public String getSafename() { return safename; }
    @Override public String getKeyname() { return keyname; }
}
