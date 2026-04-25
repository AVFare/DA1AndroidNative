package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @SerializedName("activityRating")
    private int activityRating;
    @SerializedName("guideRating")
    private int guideRating;
    @SerializedName("comment")
    private String comment;

    public ReviewRequest(int activityRating, int guideRating, String comment) {
        this.activityRating = activityRating;
        this.guideRating = guideRating;
        this.comment = comment;
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
}
