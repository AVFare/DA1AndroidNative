package com.example.da1androidnative;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.da1androidnative.data.model.ActivityHistoryResponse;
import com.example.da1androidnative.data.model.PaginatedActivityHistoryResponse;
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
    private ActivityHistoryViewModel viewModel;

    @Before
    public void setup() {
        repository = mock(ActivityHistoryRepository.class);
        viewModel = new ActivityHistoryViewModel(repository);
    }

    @Test
    public void loadHistory_successfulResponse_updatesActivities() {
        // Arrange
        List<ActivityHistoryResponse> mockHistory = new ArrayList<>();
        ActivityHistoryResponse mockItem = mock(ActivityHistoryResponse.class);
        mockHistory.add(mockItem);
        PaginatedActivityHistoryResponse mockResponse = new PaginatedActivityHistoryResponse();
        mockResponse.setContent(mockHistory);
        mockResponse.setLast(true);

        Call<PaginatedActivityHistoryResponse> mockCall = mock(Call.class);
        when(repository.getActivityHistory(
                nullable(String.class),
                nullable(String.class),
                nullable(Long.class),
                nullable(String.class),
                any(Integer.class),
                any(Integer.class)))
                .thenReturn(mockCall);

        // Act
        viewModel.loadHistory(null, null, null, "COMPLETED,CANCELLED");

        // Capturar el callback de Retrofit
        ArgumentCaptor<Callback<PaginatedActivityHistoryResponse>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockCall).enqueue(captor.capture());

        // Simular respuesta exitosa
        captor.getValue().onResponse(mockCall, Response.success(mockResponse));

        // Assert
        assertEquals(mockHistory, viewModel.activities.getValue());
        assertEquals(false, viewModel.isLoading.getValue());
    }

    @Test
    public void loadHistory_errorResponse_updatesError() {
        // Arrange
        Call<PaginatedActivityHistoryResponse> mockCall = mock(Call.class);
        when(repository.getActivityHistory(
                nullable(String.class),
                nullable(String.class),
                nullable(Long.class),
                nullable(String.class),
                any(Integer.class),
                any(Integer.class)))
                .thenReturn(mockCall);

        // Act
        viewModel.loadHistory(null, null, null, "COMPLETED,CANCELLED");

        ArgumentCaptor<Callback<PaginatedActivityHistoryResponse>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockCall).enqueue(captor.capture());

        // Simular error de red
        captor.getValue().onFailure(mockCall, new RuntimeException("Network Error"));

        // Assert
        assertNotNull(viewModel.error.getValue());
        assertEquals(false, viewModel.isLoading.getValue());
    }

    @Test
    public void loadHistory_exactlyPageSizeAndBackendMarksLast_disablesNextPage() {
        // Arrange
        List<ActivityHistoryResponse> mockHistory = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            mockHistory.add(mock(ActivityHistoryResponse.class));
        }

        PaginatedActivityHistoryResponse mockResponse = new PaginatedActivityHistoryResponse();
        mockResponse.setContent(mockHistory);
        mockResponse.setLast(true);

        Call<PaginatedActivityHistoryResponse> mockCall = mock(Call.class);
        when(repository.getActivityHistory(
                nullable(String.class),
                nullable(String.class),
                nullable(Long.class),
                nullable(String.class),
                any(Integer.class),
                any(Integer.class)))
                .thenReturn(mockCall);

        // Act
        viewModel.loadHistory(null, null, null, "COMPLETED,CANCELLED");

        ArgumentCaptor<Callback<PaginatedActivityHistoryResponse>> captor = ArgumentCaptor.forClass(Callback.class);
        verify(mockCall).enqueue(captor.capture());
        captor.getValue().onResponse(mockCall, Response.success(mockResponse));

        // Assert
        assertTrue(viewModel.isLastPage.getValue());
    }
}
