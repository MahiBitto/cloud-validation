package com.crypto.keymaker.entity;

//import jakarta.persistence.*;
import java.time.Instant;

import org.springframework.data.annotation.Id;

import com.google.cloud.spring.data.spanner.core.mapping.Column;
import com.google.cloud.spring.data.spanner.core.mapping.Table;

//@Entity 
@Table(name = "kek_info")
public class KekInfo {
    @Id @Column(name = "kek_info_id") private String kekInfoId;
    @Column(name = "onprem_kek_identifier") private String onpremKekIdentifier;
    @Column(name = "cloud_kek_identifier") private String cloudKekIdentifier;
    @Column(name = "cloud_keyring_name") private String cloudKeyringName;
    @Column(name = "kek_type") private String kekType;
    @Column(name = "is_active") private boolean isActive;
    @Column(name = "dek_cnt") private int dekCnt;
    @Column(name = "kek_algo") private String kekAlgo;
    @Column(name = "creat_ts") private Instant creatTs;
    @Column(name = "lst_updt_ts") private Instant lstUpdtTs;

    public String getKekInfoId() { return kekInfoId; }
    public void setKekInfoId(String id) { this.kekInfoId = id; }
    public String getOnpremKekIdentifier() { return onpremKekIdentifier; }
    public void setOnpremKekIdentifier(String val) { this.onpremKekIdentifier = val; }
    public String getCloudKekIdentifier() { return cloudKekIdentifier; }
    public void setCloudKekIdentifier(String val) { this.cloudKekIdentifier = val; }
    public String getKekType() { return kekType; }
    public void setKekType(String type) { this.kekType = type; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public int getDekCnt() { return dekCnt; }
    public void setDekCnt(int cnt) { this.dekCnt = cnt; }
    public Instant getCreatTs() { return creatTs; }
    public void setCreatTs(Instant ts) { this.creatTs = ts; }
    public Instant getLstUpdtTs() { return lstUpdtTs; }
    public void setLstUpdtTs(Instant ts) { this.lstUpdtTs = ts; }
}
