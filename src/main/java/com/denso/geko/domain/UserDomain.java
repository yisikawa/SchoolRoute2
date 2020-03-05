package com.denso.geko.domain;

import java.io.Serializable;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"userId", "btxId", "sensorId", "sensor2Id", "note"})
public class UserDomain implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * ユーザID
     */
    @JsonProperty("user_id")
    private String userId;
    
    /**
     * パスワード
     */
    @JsonIgnore
    private String password;
    
    /**
     * 役割
     */
    @JsonIgnore
    private String role;
    
    /**
     * BeaconTXのID
     */
    @JsonProperty("btx_id")
    private int btxId;
    
    /**
     * センサーID
     */
    @JsonProperty("sensor_id")
    private String sensorId;
    @JsonProperty("sensor2_id")
    private String sensor2Id;
    
    /**
     * メモ
     */
    private String note;
    
    /**
     * 無効印
     */
    @JsonIgnore
    private String invalid;
    
    /**
     * 最終更新日
     */
    @JsonIgnore
    private Timestamp last_modified;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getBtxId() {
        return btxId;
    }

    public void setBtxId(int btxId) {
        this.btxId = btxId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }
    
    public String getSensor2Id() {
        return sensor2Id;
    }

    public void setSensor2Id(String sensor2Id) {
        this.sensor2Id = sensor2Id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getInvalid() {
        return invalid;
    }

    public void setInvalid(String invalid) {
        this.invalid = invalid;
    }

    public Timestamp getLast_modified() {
        return last_modified;
    }

    public void setLast_modified(Timestamp last_modified) {
        this.last_modified = last_modified;
    }
}
