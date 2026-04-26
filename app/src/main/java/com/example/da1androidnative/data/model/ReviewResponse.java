package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ReviewResponse {
    @SerializedName("reviewId")
    private long reviewId;
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
    @SerializedName("activityRating")
    private int activityRating;
    @SerializedName("guideRating")
    private int guideRating;
    @SerializedName("comment")
    private String comment;
    @SerializedName("reviewedAt")
    private String reviewedAt;

    public long getReviewId() {
        return reviewId;
    }

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

    public int getActivityRating() {
        return activityRating;
    }

    public int getGuideRating() {
        return guideRating;
    }

    public String getComment() {
        return comment;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }
}
