package com.example.da1androidnative.data.model;

public class ConfirmEmailChangeRequest {
    private final String newEmail;
    private final String code;

    public ConfirmEmailChangeRequest(String newEmail, String code) {
        this.newEmail = newEmail;
        this.code = code;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public String getCode() {
        return code;
    }
}