package com.crypto.keymaker.entity;

//import jakarta.persistence.*;
import java.time.Instant;

import org.springframework.data.annotation.Id;

import com.google.cloud.spring.data.spanner.core.mapping.Column;
import com.google.cloud.spring.data.spanner.core.mapping.Table;

//@Entity 
@Table(name = "user_info")
public class UserInfo {
    //@Id @Column(name = "user_info_id", length = 36) private String userInfoId;
    @Id 
    @Column(name = "user_info_id") 
    private String userInfoId;
    private String username;
    @Column(name = "user_type") private String userType;
    @Column(name = "user_role") private String userRole;
    @Column(name = "user_plaform") private String userPlatform;
    @Column(name = "is_active") private boolean isActive;
    @Column(name = "creat_ts") private Instant creatTs;
    @Column(name = "lst_updt_ts") private Instant lstUpdtTs;

    public String getUserInfoId() { return userInfoId; }
    public void setUserInfoId(String id) { this.userInfoId = id; }
    public String getUsername() { return username; }
    public void setUsername(String name) { this.username = name; }
    public String getUserType() { return userType; }
    public void setUserType(String type) { this.userType = type; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String role) { this.userRole = role; }
    public String getUserPlatform() { return userPlatform; }
    public void setUserPlatform(String platform) { this.userPlatform = platform; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean val) { this.isActive = val; }
    public Instant getCreatTs() { return creatTs; }
    public void setCreatTs(Instant ts) { this.creatTs = ts; }
}
