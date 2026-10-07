package com.example.keymaker.model;

public record KeyMaterialRecord(
        String keyRegion,
        String cloudKekIdentifier,
        String cloudKeyringName,
        String keyMaterialJsonB64
) {
}
