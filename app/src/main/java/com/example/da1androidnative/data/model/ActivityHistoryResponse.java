package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ActivityHistoryResponse {
    @SerializedName("activityId")
    private Long activityId;
    @SerializedName("name")
    private String name;
    @SerializedName("date")
    private String date;
    @SerializedName("destination")
    private String destination;
    @SerializedName("guideName")
    private String guideName;
    @SerializedName("durationMinutes")
    private Integer durationMinutes;

    public Long getActivityId() {
        return activityId;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getDestination() {
        return destination;
    }

    public String getGuideName() {
        return guideName;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }
}
