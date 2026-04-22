package com.example.da1androidnative.data.model;

import java.util.Date;

public class ReservaResponse {

    private int reservationId;
    private String activityName;
    private String destination;
    private Date date;
    private String time;
    private int participantsCount;
    private String status;
    private String voucherCode;

    public int getReservationId() {
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
