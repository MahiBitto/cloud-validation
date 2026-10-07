package com.crypto.keymaker.dto;

public record IdentityBind(
    String username,
    String user_type, 
    String user_role,
    String user_platform 
) {}
