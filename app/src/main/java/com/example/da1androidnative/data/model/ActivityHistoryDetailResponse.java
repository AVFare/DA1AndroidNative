package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ActivityHistoryDetailResponse {
    @SerializedName("reservationId")
    private long reservationId;
    @SerializedName("activityName")
    private String activityName;
    @SerializedName("destination")
    private String destination;
    @SerializedName("date")
    private String date;
    @SerializedName("guideName")
    private String guideName;
    @SerializedName("durationMinutes")
    private Integer durationMinutes;
    @SerializedName("meetingPoint")
    private String meetingPoint;
    @SerializedName("cancellationPolicy")
    private String cancellationPolicy;
    @SerializedName("activityStars")
    private Integer activityStars;
    @SerializedName("guideStars")
    private Integer guideStars;
    @SerializedName("comment")
    private String comment;
    @SerializedName("hasRating")
    private boolean hasRating;

    public long getReservationId() { return reservationId; }
    public String getActivityName() { return activityName; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
    public String getGuideName() { return guideName; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getMeetingPoint() { return meetingPoint; }
    public String getCancellationPolicy() { return cancellationPolicy; }
    public Integer getActivityStars() { return activityStars; }
    public Integer getGuideStars() { return guideStars; }
    public String getComment() { return comment; }
    public boolean isHasRating() { return hasRating; }
}
