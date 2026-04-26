package com.example.da1androidnative.data.model;

import java.util.List;

public class UpdateUserProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String profilePhoto;
    private List<String> preferences;

    public UpdateUserProfileRequest(String firstName, String lastName, String phone,
                                    String profilePhoto, List<String> preferences) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.profilePhoto = profilePhoto;
        this.preferences = preferences;
    }
}