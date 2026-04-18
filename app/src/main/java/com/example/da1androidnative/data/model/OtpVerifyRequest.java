package com.example.da1androidnative.data.model;

public class OtpVerifyRequest {
    private final String email;
    private final String code;
    private final OtpPurpose purpose;

    public OtpVerifyRequest(String email, String code, OtpPurpose purpose) {
        this.email = email;
        this.code = code;
        this.purpose = purpose;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }

    public OtpPurpose getPurpose() {
        return purpose;
    }
}
