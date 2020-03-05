package com.denso.geko.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"btx_id", "timestamp", "major", "minor", "pos_id", "deviceid", "lat", "lon"})
public class LocationDomain implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * BeaconTXのID
     */
    @JsonProperty("btx_id")
    private int btxId;
    
    /**
     * 検出日時
     */
    @JsonProperty("timestamp")
    private long recvTimestamp;
    
    /**
     * メジャー番号
     */
    private int major;
    
    /**
     * マイナー番号
     */
    private int minor;
    
    /**
     * 位置ID
     */
    @JsonProperty("pos_id")
    private int posId;
    
    /**
     * EXBeaconのデバイスID
     */
    @JsonProperty("deviceid")
    private int beaconId;
    
    /**
     * 経度
     */
    private double lon;
    
    /**
     * 緯度
     */
    private double lat;
    
    /**
     * 検出終了時間
     */
    @JsonIgnore
    private long recvEndTimestamp;
    
    /**
     * 検出日時
     */
    private String recvDate;

    public int getBtxId() {
        return btxId;
    }

    public void setBtxId(int btxId) {
        this.btxId = btxId;
    }

    public long getRecvTimestamp() {
        return recvTimestamp;
    }

    public void setRecvTimestamp(long recvTimestamp) {
        this.recvTimestamp = recvTimestamp;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getPosId() {
        return posId;
    }

    public void setPosId(int posId) {
        this.posId = posId;
    }

    public int getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(int beaconId) {
        this.beaconId = beaconId;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public long getRecvEndTimestamp() {
        return recvEndTimestamp;
    }

    public void setRecvEndTimestamp(long recvEndTimestamp) {
        this.recvEndTimestamp = recvEndTimestamp;
    }

    public String getRecvDate() {
        return recvDate;
    }

    public void setRecvDate(String recvDate) {
        this.recvDate = recvDate;
    }
    
}
