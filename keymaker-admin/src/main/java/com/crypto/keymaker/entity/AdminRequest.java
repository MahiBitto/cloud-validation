package com.crypto.keymaker.entity;

import com.google.cloud.spring.data.spanner.core.mapping.Column;
import com.google.cloud.spring.data.spanner.core.mapping.Table;
import org.springframework.data.annotation.Id;
import java.time.Instant;

@Table(name = "admin_request")
public class AdminRequest {
    @Id @Column(name = "request_id") private String requestId;
    @Column(name = "correlation_id") private String correlationId;
    private String safename;
    private String keyname;
    @Column(name = "key_type") private String keyType;
    private String payload;
    @Column(name = "received_ts") private Instant receivedTs;

    // Getters and Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String id) { this.requestId = id; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String id) { this.correlationId = id; }
    public String getSafename() { return safename; }
    public void setSafename(String val) { this.safename = val; }
    public String getKeyname() { return keyname; }
    public void setKeyname(String val) { this.keyname = val; }
    public String getKeyType() { return keyType; }
    public void setKeyType(String type) { this.keyType = type; }
    public String getPayload() { return payload; }
    public void setPayload(String str) { this.payload = str; }
    public Instant getReceivedTs() { return receivedTs; }
    public void setReceivedTs(Instant ts) { this.receivedTs = ts; }
}
