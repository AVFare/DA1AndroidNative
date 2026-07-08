package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class CheckInScanRequest {

    @SerializedName("reservationId")
    private long reservationId;

    @SerializedName("qrContent")
    private String qrContent;

    public CheckInScanRequest(long reservationId, String qrContent) {
        this.reservationId = reservationId;
        this.qrContent = qrContent;
    }

    public long getReservationId() {
        return reservationId;
    }

    public String getQrContent() {
        return qrContent;
    }
}
