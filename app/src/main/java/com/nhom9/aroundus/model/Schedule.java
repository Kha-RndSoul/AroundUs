package com.nhom9.aroundus.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Schedule implements Serializable {
    private String scheduleId;
    private String userId;

    private String placeId;
    private String placeName;
    private String placeAddress;
    private String placeImageUrl;

    private long scheduleTimeMillis;
    private String note;

    private Timestamp createdAt;

    public Schedule() {
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public String getPlaceImageUrl() {
        return placeImageUrl;
    }

    public void setPlaceImageUrl(String placeImageUrl) {
        this.placeImageUrl = placeImageUrl;
    }

    public long getScheduleTimeMillis() {
        return scheduleTimeMillis;
    }

    public void setScheduleTimeMillis(long scheduleTimeMillis) {
        this.scheduleTimeMillis = scheduleTimeMillis;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}