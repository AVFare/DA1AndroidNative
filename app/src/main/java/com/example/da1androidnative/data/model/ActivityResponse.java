package com.example.da1androidnative.data.model;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ActivityResponse {
    @SerializedName("activityId")
    private long activityId;
    @SerializedName("name")
    private String name;
    @SerializedName("destination")
    private String destination;
    @SerializedName("category")
    private String category;
    @SerializedName("durationMinutes")
    private int durationMinutes;
    @SerializedName("availableSpots")
    private int availableSpots;
    @SerializedName("price")
    private Double basePrice;
    @SerializedName("image")
    private String firstImageUrl;

    public String getFirstImageUrl() {
        return firstImageUrl;
    }

    public long getId() {
        return activityId;
    }

    public String getName() {
        return name;
    }

    public String getDestination() {
        return destination;
    }

    public String getCategory() {
        return category;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getAvailableSpots() {
        return availableSpots;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    @SerializedName("featured")
    private boolean featured;

    public boolean isFeatured() {
        return featured;
    }
}