package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ReviewableReservationResponse {
    @SerializedName("reservationId")
    private long reservationId;
    @SerializedName("activityName")
    private String activityName;
    @SerializedName("destination")
    private String destination;
    @SerializedName("guideName")
    private String guideName;
    @SerializedName("activityDate")
    private String activityDate;
    @SerializedName("finishedAt")
    private String finishedAt;
    @SerializedName("reviewDeadline")
    private String reviewDeadline;

    public long getReservationId() {
        return reservationId;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getDestination() {
        return destination;
    }

    public String getGuideName() {
        return guideName;
    }

    public String getActivityDate() {
        return activityDate;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public String getReviewDeadline() {
        return reviewDeadline;
    }
}
