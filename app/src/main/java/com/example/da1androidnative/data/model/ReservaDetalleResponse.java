package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ReservaDetalleResponse extends ReservaResponse{

    @SerializedName("meetingPoint")
    private String meetingPoint;
    @SerializedName("totalPrice")
    private float totalPrice;
    @SerializedName("cancellationPolicy")
    private String cancellationPolicy;

    public String getMeetingPoint() {
        return meetingPoint;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public String getCancellationPolicy() {
        return cancellationPolicy;
    }
}
