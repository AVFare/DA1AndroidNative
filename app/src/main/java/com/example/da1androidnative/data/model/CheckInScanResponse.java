package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class CheckInScanResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("reservationId")
    private long reservationId;

    @SerializedName("activityName")
    private String activityName;

    @SerializedName("scannedAt")
    private String scannedAt;

    @SerializedName("message")
    private String message;

    public String getStatus() {
        return status;
    }

    public long getReservationId() {
        return reservationId;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getScannedAt() {
        return scannedAt;
    }

    public String getMessage() {
        return message;
    }
}
