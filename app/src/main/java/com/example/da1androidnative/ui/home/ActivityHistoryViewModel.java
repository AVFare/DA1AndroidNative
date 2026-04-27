package com.example.da1androidnative.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.da1androidnative.data.model.ActivityHistoryResponse;
import com.example.da1androidnative.data.model.PaginatedActivityHistoryResponse;
import com.example.da1androidnative.data.repository.ActivityHistoryRepository;

import java.util.List;

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

    @Inject
    public ActivityHistoryViewModel(ActivityHistoryRepository repository) {
        this.repository = repository;
    }

    public void loadHistory(String fromDate, String toDate, Long destinationId) {
        _isLoading.setValue(true);
        _error.setValue(null);

        repository.getActivityHistory(fromDate, toDate, destinationId).enqueue(new Callback<PaginatedActivityHistoryResponse>() {
            @Override
            public void onResponse(Call<PaginatedActivityHistoryResponse> call, Response<PaginatedActivityHistoryResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _activities.setValue(response.body().getContent());
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
}
