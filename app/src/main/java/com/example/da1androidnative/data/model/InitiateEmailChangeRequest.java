package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class InitiateEmailChangeRequest {
    @SerializedName("newEmail")
    private final String newEmail;

    public InitiateEmailChangeRequest(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getNewEmail() {
        return newEmail;
    }
}