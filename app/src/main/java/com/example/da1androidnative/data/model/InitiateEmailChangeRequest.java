package com.example.da1androidnative.data.model;

public class InitiateEmailChangeRequest {
    private final String newEmail;

    public InitiateEmailChangeRequest(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getNewEmail() {
        return newEmail;
    }
}