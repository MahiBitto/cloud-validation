package com.crypto.keymaker.dto;

import com.crypto.keymaker.dto.base.BaseKeyRequest;
import java.util.List;

public record OnboardRequest(
    String safename,
    String keyname,
    String key_type, 
    String regional_or_global, 
    List identity_list,
    byte[] transitGlobalDekRaw  
) implements BaseKeyRequest {
    @Override public String getSafename() { return safename; }
    @Override public String getKeyname() { return keyname; }
}
