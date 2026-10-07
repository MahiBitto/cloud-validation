package com.crypto.keymaker.entity;

//import jakarta.persistence.*;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import com.google.cloud.spring.data.spanner.core.mapping.Column;
import com.google.cloud.spring.data.spanner.core.mapping.Table;

//@Entity 
@Table(name = "keyspec_info")
public class KeyspecInfo {
    @Id @Column(name = "keyspec_info_id") private String keyspecInfoId;
    private String safename;
    private String keyname;
    @Column(name = "key_type") private String keyType;
    @Column(name = "regional_or_global") private String regionalOrGlobal;
    @Column(name = "key_region") private String keyRegion;
    @Column(name = "is_active") private boolean isActive;
    @Column(name = "creat_ts") private Instant creatTs;
    @Column(name = "lst_updt_ts") private Instant lstUpdtTs;
    @Version private Long version;

    public String getKeyspecInfoId() { return keyspecInfoId; }
    public void setKeyspecInfoId(String id) { this.keyspecInfoId = id; }
    public String getSafename() { return safename; }
    public void setSafename(String val) { this.safename = val; }
    public String getKeyname() { return keyname; }
    public void setKeyname(String val) { this.keyname = val; }
    public String getKeyType() { return keyType; }
    public void setKeyType(String val) { this.keyType = val; }
    public String getRegionalOrGlobal() { return regionalOrGlobal; }
    public void setRegionalOrGlobal(String val) { this.regionalOrGlobal = val; }
    public String getKeyRegion() { return keyRegion; }
    public void setKeyRegion(String val) { this.keyRegion = val; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public Instant getCreatTs() { return creatTs; }
    public void setCreatTs(Instant ts) { this.creatTs = ts; }
}
