package com.example.da1androidnative.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.da1androidnative.data.local.TokenManager;
import com.example.da1androidnative.data.model.ActivityHistoryResponse;
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
    private final TokenManager tokenManager;

    private final MutableLiveData<List<ActivityHistoryResponse>> _activities = new MutableLiveData<>();
    public LiveData<List<ActivityHistoryResponse>> activities = _activities;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>(null);
    public LiveData<String> error = _error;

    @Inject
    public ActivityHistoryViewModel(ActivityHistoryRepository repository, TokenManager tokenManager) {
        this.repository = repository;
        this.tokenManager = tokenManager;
    }

    public void loadHistory(String destination, String startDate, String endDate) {
        long userId = tokenManager.getUserId();
        if (userId == -1L) {
            _error.setValue("Usuario no identificado");
            return;
        }

        _isLoading.setValue(true);
        _error.setValue(null);

        repository.getActivityHistory(userId, destination, startDate, endDate).enqueue(new Callback<List<ActivityHistoryResponse>>() {
            @Override
            public void onResponse(Call<List<ActivityHistoryResponse>> call, Response<List<ActivityHistoryResponse>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _activities.setValue(response.body());
                } else {
                    _error.setValue("Error al cargar el historial: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ActivityHistoryResponse>> call, Throwable t) {
                _isLoading.setValue(false);
                _error.setValue("Error de red: " + t.getMessage());
            }
        });
    }
}
