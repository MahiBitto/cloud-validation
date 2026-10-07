package com.example.keymaker.repository;

import com.example.keymaker.model.KeyMaterialRecord;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.Type;
import org.springframework.stereotype.Repository;

@Repository
public class KeyMaterialRepository {

    private final DatabaseClient databaseClient;

    public KeyMaterialRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public KeyMaterialRecord findActiveKeyMaterial(
            String safeName,
            String keyName,
            String keyRegion) {

        Statement statement = Statement.newBuilder("""
                SELECT
                  ks.key_region,
                  ki.cloud_kek_identifier,
                  ki.cloud_keyring_name,
                  TO_BASE64(km.key_material_json_bytes)
                FROM key_material_info km
                JOIN keyspec_info ks
                  ON km.keyspec_info_id = ks.keyspec_info_id
                JOIN kek_info ki
                  ON km.kek_info_id = ki.kek_info_id
                WHERE ks.safename = @safeName
                  AND ks.keyname = @keyName
                  AND ks.key_region = @keyRegion
                  AND km.is_active = TRUE
                """)
                .bind("safeName").to(safeName)
                .bind("keyName").to(keyName)
                .bind("keyRegion").to(keyRegion)
                .build();

        try (ResultSet resultSet =
                     databaseClient.singleUse().executeQuery(statement)) {

            if (!resultSet.next()) {
                return null;
            }

            String region = resultSet.getString(0);
            String cloudKekIdentifier = resultSet.getString(1);
            String cloudKeyringName = resultSet.getString(2);
            String keyMaterialJsonB64 = resultSet.getString(3);

            return new KeyMaterialRecord(
                    region,
                    cloudKekIdentifier,
                    cloudKeyringName,
                    keyMaterialJsonB64
            );
        }
    }
}
