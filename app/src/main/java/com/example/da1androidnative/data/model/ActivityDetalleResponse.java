package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ActivityDetalleResponse extends  ActivityResponse{

    @SerializedName("shortDescription")
    private String shortDescription;
    @SerializedName("fullDescription")
    private String fullDescription;
    @SerializedName("guideName")
    private String guideName;
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


    public String getFullDescription() {
        return fullDescription;
    }

    public String getGuideName() {
        return guideName;
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

    public String getShortDescription() {
        return shortDescription;
    }
}
