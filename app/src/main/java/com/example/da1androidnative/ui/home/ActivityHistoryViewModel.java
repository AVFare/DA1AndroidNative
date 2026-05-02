package com.example.da1androidnative.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.da1androidnative.data.model.ActivityHistoryResponse;
import com.example.da1androidnative.data.model.PaginatedActivityHistoryResponse;
import com.example.da1androidnative.data.repository.ActivityHistoryRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@HiltViewModel
public class ActivityHistoryViewModel extends ViewModel {

    private final ActivityHistoryRepository repository;

    private final MutableLiveData<List<ActivityHistoryResponse>> _activities = new MutableLiveData<>();
    public LiveData<List<ActivityHistoryResponse>> activities = _activities;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>(null);
    public LiveData<String> error = _error;

    private final MutableLiveData<Integer> _currentPage = new MutableLiveData<>(0);
    public LiveData<Integer> currentPage = _currentPage;

    private final MutableLiveData<Boolean> _isLastPage = new MutableLiveData<>(false);
    public LiveData<Boolean> isLastPage = _isLastPage;

    private static final int PAGE_SIZE = 6;

    // Cache para los filtros actuales
    private String currentFromDate;
    private String currentToDate;
    private Long currentDestinationId;
    private List<String> currentStatus;
    private String selectedDestinationName = "Todos";
    private String selectedStatusDisplay = "Todos";
    private boolean historyLoaded = false;

    @Inject
    public ActivityHistoryViewModel(ActivityHistoryRepository repository) {
        this.repository = repository;
    }

    public void loadHistory(String fromDate, String toDate, Long destinationId, String status) {
        this.currentFromDate = fromDate;
        this.currentToDate = toDate;
        this.currentDestinationId = destinationId;
        this.currentStatus = parseStatuses(status);
        _currentPage.setValue(0);
        fetchPage(0);
    }

    public void nextPage() {
        if (!Boolean.TRUE.equals(_isLastPage.getValue())) {
            int next = (_currentPage.getValue() != null ? _currentPage.getValue() : 0) + 1;
            _currentPage.setValue(next);
            fetchPage(next);
        }
    }

    public void previousPage() {
        int current = (_currentPage.getValue() != null ? _currentPage.getValue() : 0);
        if (current > 0) {
            int prev = current - 1;
            _currentPage.setValue(prev);
            fetchPage(prev);
        }
    }

    private void fetchPage(int page) {
        _isLoading.setValue(true);
        _error.setValue(null);

        repository.getActivityHistory(currentFromDate, currentToDate, currentDestinationId, currentStatus, page, PAGE_SIZE)
                .enqueue(new Callback<PaginatedActivityHistoryResponse>() {
            @Override
            public void onResponse(Call<PaginatedActivityHistoryResponse> call, Response<PaginatedActivityHistoryResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    PaginatedActivityHistoryResponse body = response.body();
                    List<ActivityHistoryResponse> content = body.getContent();
                    _activities.setValue(content != null ? content : new ArrayList<>());
                    historyLoaded = true;

                    if (body.getNumber() != null) {
                        _currentPage.setValue(body.getNumber());
                    }
                    _isLastPage.setValue(isLastPage(body, content, page));
                } else {
                    _error.setValue("Error al cargar el historial: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PaginatedActivityHistoryResponse> call, Throwable t) {
                _isLoading.setValue(false);
                _error.setValue("Error de red: " + t.getMessage());
            }
        });
    }

    public boolean hasHistoryLoaded() {
        return historyLoaded;
    }

    public String getCurrentFromDate() {
        return currentFromDate;
    }

    public String getCurrentToDate() {
        return currentToDate;
    }

    public Long getCurrentDestinationId() {
        return currentDestinationId;
    }

    public String getSelectedDestinationName() {
        return selectedDestinationName;
    }

    public String getSelectedStatusDisplay() {
        return selectedStatusDisplay;
    }

    public void setSelectedFilters(String destinationName, String statusDisplay) {
        selectedDestinationName = destinationName != null && !destinationName.trim().isEmpty()
                ? destinationName
                : "Todos";
        selectedStatusDisplay = statusDisplay != null && !statusDisplay.trim().isEmpty()
                ? statusDisplay
                : "Todos";
    }

    private boolean isLastPage(PaginatedActivityHistoryResponse body, List<ActivityHistoryResponse> content, int page) {
        if (body.getLast() != null) {
            return body.getLast();
        }

        Integer totalPages = body.getTotalPages();
        if (totalPages != null) {
            return page >= totalPages - 1;
        }

        return content == null || content.size() < PAGE_SIZE;
    }

    private List<String> parseStatuses(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }

        List<String> statuses = Arrays.stream(status.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
        return statuses.isEmpty() ? null : statuses;
    }
}
