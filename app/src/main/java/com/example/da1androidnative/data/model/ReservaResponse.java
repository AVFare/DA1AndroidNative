package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class ReservaResponse {

    @SerializedName("reservationId")
    private long reservationId;
    
    // Algunos backs mandan activityId, otros mandan el objeto activity
    @SerializedName("activityId")
    private long activityId;
    
    @SerializedName("activity")
    private ActivityResponse activity;

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

    public long getReservationId() { return reservationId; }

    public void setReservationId(long reservationId) { this.reservationId = reservationId; }
    
    public long getActivityId() { 
        // Si activityId es 0, intentamos sacarlo del objeto activity
        if (activityId == 0 && activity != null) {
            return activity.getId();
        }
        return activityId; 
    }

    public void setActivityId(long activityId) { this.activityId = activityId; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getParticipantsCount() { return participantsCount; }
    public void setParticipantsCount(int participantsCount) { this.participantsCount = participantsCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
}
