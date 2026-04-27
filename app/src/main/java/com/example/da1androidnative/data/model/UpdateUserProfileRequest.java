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
    private String profilePhoto;

    public UpdateUserProfileRequest(String firstName, String lastName, String phone, String profilePhoto) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.profilePhoto = profilePhoto;
    }

}