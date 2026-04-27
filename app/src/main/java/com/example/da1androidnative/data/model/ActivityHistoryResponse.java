package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ActivityHistoryResponse {
    @SerializedName("reservationId")
    private long reservationId;
    @SerializedName("activityName")
    private String activityName;
    @SerializedName("destination")
    private String destination;
    @SerializedName("date")
    private String date;
    @SerializedName("guideName")
    private String guideName;
    @SerializedName("durationMinutes")
    private Integer durationMinutes;
    @SerializedName("rating")
    private Integer rating;
    @SerializedName("hasRating")
    private boolean hasRating;

    public long getReservationId() {
        return reservationId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGuideName() {
        return guideName;
    }

    public void setGuideName(String guideName) {
        this.guideName = guideName;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public boolean isHasRating() {
        return hasRating;
    }

    public void setHasRating(boolean hasRating) {
        this.hasRating = hasRating;
    }
}
