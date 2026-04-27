package com.example.da1androidnative;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.da1androidnative.data.local.TokenManager;
import com.example.da1androidnative.data.model.ActivityHistoryResponse;
import com.example.da1androidnative.data.repository.ActivityHistoryRepository;
import com.example.da1androidnative.ui.home.ActivityHistoryViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityHistoryViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ActivityHistoryRepository repository;
    private TokenManager tokenManager;
    private ActivityHistoryViewModel viewModel;

    @Before
    public void setup() {
        repository = mock(ActivityHistoryRepository.class);
        tokenManager = mock(TokenManager.class);
        viewModel = new ActivityHistoryViewModel(repository, tokenManager);
    }

    @Test
    public void loadHistory_successfulResponse_updatesActivities() {
        // Arrange
        long userId = 5L;
        when(tokenManager.getUserId()).thenReturn(userId);
        
        List<ActivityHistoryResponse> mockHistory = new ArrayList<>();
        ActivityHistoryResponse mockItem = mock(ActivityHistoryResponse.class);
        mockHistory.add(mockItem);

        Call<List<ActivityHistoryResponse>> mockCall = mock(Call.class);
        // Usar nullable(String.class) para permitir nulls
        when(repository.getActivityHistory(nullable(String.class), nullable(String.class), nullable(String.class)))
                .thenReturn(mockCall);

        // Act
        viewModel.loadHistory(null, null, null);

        // Capturar el callback de Retrofit
        ArgumentCaptor<Callback<List<ActivityHistoryResponse>>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockCall).enqueue(captor.capture());

        // Simular respuesta exitosa
        captor.getValue().onResponse(mockCall, Response.success(mockHistory));

        // Assert
        assertEquals(mockHistory, viewModel.activities.getValue());
        assertEquals(false, viewModel.isLoading.getValue());
    }

    @Test
    public void loadHistory_errorResponse_updatesError() {
        // Arrange
        when(tokenManager.getUserId()).thenReturn(1L);
        Call<List<ActivityHistoryResponse>> mockCall = mock(Call.class);
        when(repository.getActivityHistory(nullable(String.class), nullable(String.class), nullable(String.class)))
                .thenReturn(mockCall);

        // Act
        viewModel.loadHistory(null, null, null);

        ArgumentCaptor<Callback<List<ActivityHistoryResponse>>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockCall).enqueue(captor.capture());

        // Simular error de red
        captor.getValue().onFailure(mockCall, new RuntimeException("Network Error"));

        // Assert
        assertNotNull(viewModel.error.getValue());
        assertEquals(false, viewModel.isLoading.getValue());
    }
}
