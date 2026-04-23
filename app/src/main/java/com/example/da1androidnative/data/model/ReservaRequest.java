package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ReservaRequest {
    @SerializedName("activityId")
    private long activityId;
    @SerializedName("scheduleId")
    private long scheduleId;
    @SerializedName("participantsCount")
    private int participantsCount;

    public ReservaRequest(long activityId, long scheduleId, int participantsCount) {
        this.activityId = activityId;
        this.scheduleId = scheduleId;
        this.participantsCount = participantsCount;
    }

    public long getActivityId() {
        return activityId;
    }

    public void setActivityId(long activityId) {
        this.activityId = activityId;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getParticipantsCount() {
        return participantsCount;
    }

    public void setParticipantsCount(int participantsCount) {
        this.participantsCount = participantsCount;
    }
}
