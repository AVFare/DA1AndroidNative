package com.example.da1androidnative.data.repository;

import com.example.da1androidnative.data.model.ActivityHistoryResponse;
import com.example.da1androidnative.data.network.ApiService;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class ActivityHistoryRepository {
    private final ApiService apiService;

    @Inject
    public ActivityHistoryRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<List<ActivityHistoryResponse>> getActivityHistory(long userId, String destination, String startDate, String endDate) {
        return apiService.getActivityHistory(userId, destination, startDate, endDate);
    }
}
