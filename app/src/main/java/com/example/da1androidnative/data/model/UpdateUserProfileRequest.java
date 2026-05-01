package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UpdateUserProfileRequest {

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("phone")
    private String phone;

    @SerializedName("profilePhoto")
    private String profilePictureUrl;

    @SerializedName("preferences")
    private List<String> preferences;

    public UpdateUserProfileRequest(
            String firstName,
            String lastName,
            String phone,
            String profilePictureUrl,
            List<String> preferences
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.profilePictureUrl = profilePictureUrl;
        this.preferences = preferences;
    }

    public List<String> getPreferences() { return preferences; }
    public void setPreferences(List<String> preferences) { this.preferences = preferences; }
}