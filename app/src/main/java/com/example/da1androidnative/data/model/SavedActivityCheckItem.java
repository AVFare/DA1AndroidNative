package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class SavedActivityCheckItem {
    @SerializedName("activityId")
    private long activityId;

    @SerializedName("price")
    private Double price;

    @SerializedName("availableSpots")
    private Integer availableSpots;

    @SerializedName("currency")
    private String currency;

    public long getActivityId() {
        return activityId;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getAvailableSpots() {
        return availableSpots;
    }

    public String getCurrency() {
        return currency;
    }
}
