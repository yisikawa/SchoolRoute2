package com.denso.geko.domain;

import java.io.Serializable;
import java.sql.Timestamp;

public class GeometryDomain implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private int id;
    
    /**
     * 種別(1:学校ビーコン、2：交差点ビーコン、3：その他マーカー
     */
    private String type;
    
    /**
     * 経度
     */
    private double lon;
    
    /**
     * 緯度
     */
    private double lat;
    
    /**
     * マーカー種類(色)
     */
    private String markerType;
    
    /**
     * マーカーアイコン
     */
    private String markerIcon;
    
    /**
     * マーカーコメント
     */
    private String markerComment;
    
    /**
     * メモ
     */
    private String note;
    
    /**
     * 無効印
     */
    private String invalid;
    
    /**
     * 登録者ユーザID
     */
    private String userId;
    
    /**
     * 最終更新日
     */
    private Timestamp last_modified;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getMarkerType() {
        return markerType;
    }

    public void setMarkerType(String markerType) {
        this.markerType = markerType;
    }

    public String getMarkerIcon() {
        return markerIcon;
    }

    public void setMarkerIcon(String markerIcon) {
        this.markerIcon = markerIcon;
    }

    public String getMarkerComment() {
        return markerComment;
    }

    public void setMarkerComment(String markerComment) {
        this.markerComment = markerComment;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getLast_modified() {
        return last_modified;
    }

    public void setLast_modified(Timestamp last_modified) {
        this.last_modified = last_modified;
    }
}
