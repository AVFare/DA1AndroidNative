package com.example.da1androidnative.data.model;
import com.google.gson.annotations.SerializedName;
public class AuthResponse {
    @SerializedName("token")
    private String token;
    public AuthResponse() {}
    public String getToken() { return token; }
    public void setToken(String token) {
        this.token = token;
    }
}
