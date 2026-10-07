package com.crypto.keymaker.entity;

//import jakarta.persistence.*;
import java.time.Instant;

import org.springframework.data.annotation.Id;

import com.google.cloud.spring.data.spanner.core.mapping.Column;
import com.google.cloud.spring.data.spanner.core.mapping.Table;

//@Entity 
@Table(name = "key_material_info")
public class KeyMaterialInfo {
    @Id @Column(name = "key_material_info_id") private String keyMaterialInfoId;
    @Column(name = "keyspec_info_id") private String keyspecInfoId;
    @Column(name = "kek_info_id") private String kekInfoId;
    //@Lob @Column(name = "key_material_json_bytes") private byte[] keyMaterialJsonBytes;
     // REMOVED @Lob annotation line completely
    @Column(name = "key_material_json_bytes") 
    private byte[] keyMaterialJsonBytes;
    @Column(name = "is_active") private boolean isActive;
    @Column(name = "creat_ts") private Instant creatTs;
    @Column(name = "lst_updt_ts") private Instant lstUpdtTs;

    public String getKeyMaterialInfoId() { return keyMaterialInfoId; }
    public void setKeyMaterialInfoId(String id) { this.keyMaterialInfoId = id; }
    public String getKeyspecInfoId() { return keyspecInfoId; }
    public void setKeyspecInfoId(String id) { this.keyspecInfoId = id; }
    public String getKekInfoId() { return kekInfoId; }
    public void setKekInfoId(String id) { this.kekInfoId = id; }
    public byte[] getKeyMaterialJsonBytes() { return keyMaterialJsonBytes; }
    public void setKeyMaterialJsonBytes(byte[] bytes) { this.keyMaterialJsonBytes = bytes; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public Instant getCreatTs() { return creatTs; }
    public void setCreatTs(Instant ts) { this.creatTs = ts; }
}
