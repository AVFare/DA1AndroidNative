package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ReviewResponse {
    @SerializedName("ratingId")
    private long ratingId;
    @SerializedName("reservationId")
    private long reservationId;
    @SerializedName("activityStars")
    private int activityStars;
    @SerializedName("guideStars")
    private int guideStars;
    @SerializedName("comment")
    private String comment;
    @SerializedName("createdAt")
    private String createdAt;

    public long getRatingId() {
        return ratingId;
    }

    public long getReservationId() {
        return reservationId;
    }

    public int getActivityStars() {
        return activityStars;
    }

    public int getGuideStars() {
        return guideStars;
    }

    public String getComment() {
        return comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
