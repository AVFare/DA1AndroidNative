package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ScheduleResponse {
    @SerializedName("scheduleId")
    private long scheduleId;
    @SerializedName("date")
    private String date;
    @SerializedName("time")
    private String time;
    @SerializedName("availableSpots")
    private int availableSpots;
    @SerializedName("totalSpots")
    private int totalSpots;

    public long getScheduleId() {
        return scheduleId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getAvailableSpots() {
        return availableSpots;
    }

    public int getTotalSpots() {
        return totalSpots;
    }
}
