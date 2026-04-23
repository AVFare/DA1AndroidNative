package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ReservaRequest {
    @SerializedName("activityId")
    private long activityId;
    @SerializedName("scheduleId")
    private long scheduleId;
    @SerializedName("participantsCount")
    private int participantsCount;
}
