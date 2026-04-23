package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ReservaCancelledResponse {
    @SerializedName("reservationId")
    private long reservationId;
    @SerializedName("status")
    private String status;
    @SerializedName("cancelledAt")
    private String cancelledAt;
    @SerializedName("message")
    private String message;

    public long getReservationId() {
        return reservationId;
    }

    public String getStatus() {
        return status;
    }

    public String getCancelledAt() {
        return cancelledAt;
    }

    public String getMessage() {
        return message;
    }
}
