package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReservaDetalleResponse extends ReservaResponse {

    @SerializedName("meetingPoint")
    private String meetingPoint;
    @SerializedName("meetingPointLatitude")
    private Double meetingPointLatitude;
    @SerializedName("meetingPointLongitude")
    private Double meetingPointLongitude;
    @SerializedName("totalPrice")
    private float totalPrice;
    @SerializedName("cancellationPolicy")
    private String cancellationPolicy;
    @SerializedName("itineraries")
    private List<ItineraryResponse> itineraries;

    public String getMeetingPoint() {
        return meetingPoint;
    }

    public Double getMeetingPointLatitude() {
        return meetingPointLatitude;
    }

    public Double getMeetingPointLongitude() {
        return meetingPointLongitude;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public String getCancellationPolicy() {
        return cancellationPolicy;
    }

    public List<ItineraryResponse> getItineraries() {
        return itineraries;
    }
}