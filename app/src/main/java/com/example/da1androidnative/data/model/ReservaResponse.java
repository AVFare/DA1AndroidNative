package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ReservaResponse {

    @SerializedName("reservationId")
    private Long reservationId;
    @SerializedName("activityName")
    private String activityName;
    @SerializedName("destination")
    private String destination;
    @SerializedName("date")
    private Date date;
    @SerializedName("time")
    private String time;
    @SerializedName("participantsCount")
    private int participantsCount;
    @SerializedName("status")
    private String status;
    @SerializedName("voucherCode")
    private String voucherCode;

    public Long getReservationId() {
        return reservationId;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getDestination() {
        return destination;
    }

    public Date getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getParticipantsCount() {
        return participantsCount;
    }

    public String getStatus() {
        return status;
    }

    public String getVoucherCode() {
        return voucherCode;
    }
}
