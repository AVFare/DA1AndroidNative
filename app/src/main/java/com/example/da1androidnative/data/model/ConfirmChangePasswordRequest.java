package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

public class ConfirmChangePasswordRequest {
    @SerializedName("code")
    private String code;
    @SerializedName("newPassword")
    private String newPassword;

    public ConfirmChangePasswordRequest(String code, String newPassword) {
        this.code = code;
        this.newPassword = newPassword;
    }

    public String getCode() {
        return code;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
