package com.crypto.keymaker.entity;

//import jakarta.persistence.*;
import java.time.Instant;

import org.springframework.data.annotation.Id;

import com.google.cloud.spring.data.spanner.core.mapping.Column;
import com.google.cloud.spring.data.spanner.core.mapping.Table;

//@Entity 
@Table(name = "keyspec_binding")
public class KeyspecBinding {
    @Id @Column(name = "keyspec_binding_id") private String keyspecBindingId;
    @Column(name = "keyspec_info_id") private String keyspecInfoId;
    @Column(name = "user_info_id") private String userInfoId;
    @Column(name = "is_active") private boolean isActive;
    @Column(name = "creat_ts") private Instant creatTs;
    @Column(name = "lst_updt_ts") private Instant lstUpdtTs;

    public String getKeyspecBindingId() { return keyspecBindingId; }
    public void setKeyspecBindingId(String id) { this.keyspecBindingId = id; }
    public String getKeyspecInfoId() { return keyspecInfoId; }
    public void setKeyspecInfoId(String id) { this.keyspecInfoId = id; }
    public String getUserInfoId() { return userInfoId; }
    public void setUserInfoId(String id) { this.userInfoId = id; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean val) { this.isActive = val; }
    public Instant getCreatTs() { return creatTs; }
    public void setCreatTs(Instant ts) { this.creatTs = ts; }
}
