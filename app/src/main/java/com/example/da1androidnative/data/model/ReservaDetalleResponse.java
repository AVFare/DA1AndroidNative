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

    public static ReservaDetalleResponse fromSummary(ReservaResponse reservation) {
        ReservaDetalleResponse detail = new ReservaDetalleResponse();
        detail.setReservationId(reservation.getReservationId());
        detail.setActivityId(reservation.getActivityId());
        detail.setActivityName(reservation.getActivityName());
        detail.setDestination(reservation.getDestination());
        detail.setDate(reservation.getDate());
        detail.setTime(reservation.getTime());
        detail.setParticipantsCount(reservation.getParticipantsCount());
        detail.setStatus(reservation.getStatus());
        detail.setVoucherCode(reservation.getVoucherCode());
        return detail;
    }

    public String getMeetingPoint() {
        return meetingPoint;
    }

    public void setMeetingPoint(String meetingPoint) {
        this.meetingPoint = meetingPoint;
    }

    public Double getMeetingPointLatitude() {
        return meetingPointLatitude;
    }

    public void setMeetingPointLatitude(Double meetingPointLatitude) {
        this.meetingPointLatitude = meetingPointLatitude;
    }

    public Double getMeetingPointLongitude() {
        return meetingPointLongitude;
    }

    public void setMeetingPointLongitude(Double meetingPointLongitude) {
        this.meetingPointLongitude = meetingPointLongitude;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCancellationPolicy() {
        return cancellationPolicy;
    }

    public void setCancellationPolicy(String cancellationPolicy) {
        this.cancellationPolicy = cancellationPolicy;
    }

    public List<ItineraryResponse> getItineraries() {
        return itineraries;
    }

    public void setItineraries(List<ItineraryResponse> itineraries) {
        this.itineraries = itineraries;
    }
}
