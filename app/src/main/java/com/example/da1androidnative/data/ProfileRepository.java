package com.example.da1androidnative.data;

import com.example.da1androidnative.data.model.UpdateUserPreferencesRequest;
import com.example.da1androidnative.data.model.UpdateUserProfileRequest;
import com.example.da1androidnative.data.model.UserProfileResponse;
import com.example.da1androidnative.data.network.ApiService;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class ProfileRepository {

    private final ApiService apiService;

    @Inject
    public ProfileRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<UserProfileResponse> getMyProfile() {
        return apiService.getMyProfile();
    }

    public Call<UserProfileResponse> updateMyProfile(UpdateUserProfileRequest request) {
        return apiService.updateMyProfile(request);
    }

    public Call<UserProfileResponse> updateMyPreferences(UpdateUserPreferencesRequest request) {
        return apiService.updateMyPreferences(request);
    }
}