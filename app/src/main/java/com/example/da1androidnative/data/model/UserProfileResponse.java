package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserProfileResponse {

    @SerializedName("userId")
    private long userId;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("profilePictureUrl")
    private String profilePictureUrl;

    @SerializedName("preferences")
    private List<String> travelPreferences;

    @SerializedName("reservedActivitiesCount")
    private long reservedActivitiesCount;

    @SerializedName("completedActivitiesCount")
    private long completedActivitiesCount;

    @SerializedName("reservationSummary")
    private ReservationSummary reservationSummary;

    @SerializedName("profilePhoto")
    private String profilePhoto;

    public String getProfilePhoto() { return profilePhoto; }
    public long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public List<String> getTravelPreferences() { return travelPreferences; }
    public long getReservedActivitiesCount() { return this.reservationSummary.getConfirmed(); }
    public long getCompletedActivitiesCount() { return this.reservationSummary.getCompleted(); }
}