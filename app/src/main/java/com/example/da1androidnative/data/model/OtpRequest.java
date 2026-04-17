package com.example.da1androidnative.data.model;

public class OtpRequest {
    private final String email;
    private final OtpPurpose purpose;

    public OtpRequest(String email, OtpPurpose purpose) {
        this.email = email;
        this.purpose = purpose;
    }

    public String getEmail() {
        return email;
    }

    public OtpPurpose getPurpose() {
        return purpose;
    }
}
