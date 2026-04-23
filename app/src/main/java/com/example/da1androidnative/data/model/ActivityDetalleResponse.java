package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ActivityDetalleResponse extends  ActivityResponse{
    @SerializedName("category")
    private String category;
    @SerializedName("fullDescription")
    private String fullDescription;
    @SerializedName("destination")
    private String destination;
    @SerializedName("guideName")
    private String guideName;
    @SerializedName("durationMinutes")
    private int durationMinutes;
    @SerializedName("language")
    private String language;
    @SerializedName("meetingPoint")
    private String meetingPoint;
    @SerializedName("inclusions")
    private String inclusions;
    @SerializedName("cancellationPolicy")
    private String cancellationPolicy;
    @SerializedName("currency")
    private String currency;
    @SerializedName("availableSpots")
    private int availableSpots;

    public String getCategory() {
        return category;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public String getDestination() {
        return destination;
    }

    public String getGuideName() {
        return guideName;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getLanguage() {
        return language;
    }

    public String getMeetingPoint() {
        return meetingPoint;
    }

    public String getInclusions() {
        return inclusions;
    }

    public String getCancellationPolicy() {
        return cancellationPolicy;
    }

    public String getCurrency() {
        return currency;
    }

    public int getAvailableSpots() {
        return availableSpots;
    }
}
