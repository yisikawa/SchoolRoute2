package com.denso.geko.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"cuserId", "ratingDate", "rating", "shortCutCnt", "detourCnt", "wrongWayCnt", "elapsedTime", "markerCnt"})
public class RatingDomain implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /**
     * 親のユーザID
     */
    @JsonIgnore
    private String userId;
    
    /**
     * 評価対象児童のユーザID
     */
    private String cuserId;
    
    /**
     * 評価対象の日付
     */
    private String ratingDate;
    /**
     * 評価値
     */
    private Integer rating;
    
    /**
     * 近道した交差点数
     */
    private Integer shortCutCnt;
    
    /**
     * 寄り道した交差点数
     */
    private Integer detourCnt;
    
    /**
     * 逆走した交差点数
     */
    private Integer wrongWayCnt;
    
    /**
     * 下校にかかった時間
     */
    private Integer elapsedTime;
    
    /**
     * 地図上にメモを登録した件数
     */
    private int markerCnt;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCuserId() {
        return cuserId;
    }

    public void setCuserId(String cuserId) {
        this.cuserId = cuserId;
    }

    public String getRatingDate() {
        return ratingDate;
    }

    public void setRatingDate(String ratingDate) {
        this.ratingDate = ratingDate;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getShortCutCnt() {
        return shortCutCnt;
    }

    public void setShortCutCnt(Integer shortCutCnt) {
        this.shortCutCnt = shortCutCnt;
    }

    public Integer getDetourCnt() {
        return detourCnt;
    }

    public void setDetourCnt(Integer detourCnt) {
        this.detourCnt = detourCnt;
    }

    public Integer getWrongWayCnt() {
        return wrongWayCnt;
    }

    public void setWrongWayCnt(Integer wrongWayCnt) {
        this.wrongWayCnt = wrongWayCnt;
    }

    public Integer getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Integer elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getMarkerCnt() {
        return markerCnt;
    }

    public void setMarkerCnt(int markerCnt) {
        this.markerCnt = markerCnt;
    }
}
