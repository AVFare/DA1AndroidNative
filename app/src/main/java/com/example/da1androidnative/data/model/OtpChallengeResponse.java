package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class OtpChallengeResponse {
    @SerializedName("email")
    private String email;

    @SerializedName("purpose")
    private String purpose;

    @SerializedName("expiresInSeconds")
    private long expiresInSeconds;

    @SerializedName("message")
    private String message;

    public String getEmail() {
        return email;
    }

    public String getPurpose() {
        return purpose;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public String getMessage() {
        return message;
    }
}
