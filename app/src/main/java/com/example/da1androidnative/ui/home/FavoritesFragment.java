package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.da1androidnative.ui.util.ToastHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.FavoritesManager;
import com.example.da1androidnative.data.model.ActivityResponse;
import com.example.da1androidnative.data.model.PaginatedActivitiesResponse;
import com.example.da1androidnative.data.model.SavedActivityCheckResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.ui.home.adapter.ActivityAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class FavoritesFragment extends Fragment implements ActivityAdapter.OnActivityClickListener {

    private static final int FAVORITES_PAGE_SIZE = 50;

    @Inject ApiService apiService;
    @Inject FavoritesManager favoritesManager;
    
    private ActivityAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        emptyState = view.findViewById(R.id.emptyFavoritesState);
        setupToolbar(view);
        setupRecyclerView(view);
        loadFavorites();
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.favoritesToolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
        }
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActivityAdapter(getContext(), favoritesManager, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadFavorites() {
        Set<String> favIds = favoritesManager.getFavoriteIds();
        if (favIds.isEmpty()) {
            adapter.setActivities(new ArrayList<>());
            updateUI(true);
            return;
        }

        loadFavoritePage(0, favIds, new ArrayList<>());
    }

    private void loadFavoritePage(int page, Set<String> favIds, List<ActivityResponse> favoriteActivities) {
        apiService.getAllActivities(null, page, FAVORITES_PAGE_SIZE).enqueue(new Callback<PaginatedActivitiesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedActivitiesResponse> call, @NonNull Response<PaginatedActivitiesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PaginatedActivitiesResponse body = response.body();
                    List<ActivityResponse> pageActivities = body.getContent();

                    if (pageActivities != null) {
                        for (ActivityResponse activity : pageActivities) {
                            if (favIds.contains(String.valueOf(activity.getId()))) {
                                favoritesManager.saveSnapshot(activity);
                                favoriteActivities.add(activity);
                            }
                        }
                    }

                    if (isLastPage(body, pageActivities, page)) {
                        adapter.setActivities(favoriteActivities);
                        updateUI(favoriteActivities.isEmpty());
                        checkFavoriteUpdates();
                    } else {
                        loadFavoritePage(page + 1, favIds, favoriteActivities);
                    }
                } else {
                    ToastHelper.show(getContext(), "No se pudieron cargar favoritos");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedActivitiesResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    ToastHelper.show(getContext(), "Error de red: " + t.getMessage());
                }
            }
        });
    }

    private void checkFavoriteUpdates() {
        String ids = favoritesManager.getFavoriteIdsCsv();
        if (ids.isEmpty()) return;

        apiService.checkSavedActivities(ids).enqueue(new Callback<SavedActivityCheckResponse>() {
            @Override
            public void onResponse(@NonNull Call<SavedActivityCheckResponse> call,
                                   @NonNull Response<SavedActivityCheckResponse> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                favoritesManager.applyBatchState(response.body().getContent());
                adapter.refreshFavorites();
            }

            @Override
            public void onFailure(@NonNull Call<SavedActivityCheckResponse> call,
                                  @NonNull Throwable t) {
                if (isAdded()) {
                    ToastHelper.show(getContext(), "Error al validar favoritos");
                }
            }
        });
    }

    private boolean isLastPage(PaginatedActivitiesResponse body, List<ActivityResponse> content, int page) {
        if (body.getLast() != null) {
            return body.getLast();
        }
        Integer totalPages = body.getTotalPages();
        if (totalPages != null) {
            return page >= totalPages - 1;
        }
        return content == null || content.size() < FAVORITES_PAGE_SIZE;
    }

    private void updateUI(boolean isEmpty) {
        if (emptyState != null) {
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onActivityClick(Long activityId) {
        Bundle args = new Bundle();
        args.putLong("activityId", activityId);
        NavHostFragment.findNavController(this).navigate(R.id.action_favorites_to_activityDetalleFragment, args);
    }

    @Override
    public void onFavoriteClick(ActivityResponse activity) {
        favoritesManager.toggleFavorite(activity.getId());
        loadFavorites();
    }
}
