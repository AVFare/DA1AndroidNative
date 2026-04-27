package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @SerializedName("reservationId")
    private long reservationId;
    @SerializedName("activityStars")
    private int activityStars;
    @SerializedName("guideStars")
    private int guideStars;
    @SerializedName("comment")
    private String comment;

    public ReviewRequest(long reservationId, int activityStars, int guideStars, String comment) {
        this.reservationId = reservationId;
        this.activityStars = activityStars;
        this.guideStars = guideStars;
        this.comment = comment;
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
}
