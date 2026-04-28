package com.example.da1androidnative.data.repository;

import com.example.da1androidnative.data.model.PaginatedActivityHistoryResponse;
import com.example.da1androidnative.data.network.ApiService;

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

    public Call<PaginatedActivityHistoryResponse> getActivityHistory(String fromDate, String toDate, Long destinationId, String status, Integer page, Integer size) {
        return apiService.getActivityHistory(fromDate, toDate, destinationId, status, page, size);
    }
}
