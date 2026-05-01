package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ActivityDetalleResponse extends ActivityResponse {

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
    @SerializedName("meetingPointLatitude")
    private Double meetingPointLatitude;
    @SerializedName("meetingPointLongitude")
    private Double meetingPointLongitude;
    @SerializedName("inclusions")
    private String inclusions;
    @SerializedName("cancellationPolicy")
    private String cancellationPolicy;
    @SerializedName("currency")
    private String currency;
    @SerializedName("itineraries")
    private List<ItineraryResponse> itineraries;

    @SerializedName("gallery")
    private List<String> gallery;

    public String getShortDescription() {
        return shortDescription;
    }

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

    public Double getMeetingPointLatitude() {
        return meetingPointLatitude;
    }

    public Double getMeetingPointLongitude() {
        return meetingPointLongitude;
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

    public List<ItineraryResponse> getItineraries() {
        return itineraries;
    }
    public List<String> getGallery() {
        return gallery;
    }
}