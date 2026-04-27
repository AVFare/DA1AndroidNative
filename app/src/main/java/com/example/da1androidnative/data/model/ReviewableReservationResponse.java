package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ReviewableReservationResponse {
    @SerializedName("reservationId")
    private long reservationId;
    @SerializedName("activityName")
    private String activityName;
    @SerializedName("completedAt")
    private String completedAt;
    @SerializedName("expiresAt")
    private String expiresAt;

    public long getReservationId() {
        return reservationId;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}
