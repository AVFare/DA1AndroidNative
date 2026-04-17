package com.example.da1androidnative.data.network;

import com.example.da1androidnative.data.model.AuthResponse;
import com.example.da1androidnative.data.model.LoginRequest;
import com.example.da1androidnative.data.model.RegisterRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);
    @GET("activities")
    Call<List<ActivityResponse>> getActivities();
}