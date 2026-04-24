package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.FavoritesManager;
import com.example.da1androidnative.data.model.ActivityResponse;
import com.example.da1androidnative.data.model.PaginatedActivitiesResponse;
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

    @Inject ApiService apiService;
    @Inject FavoritesManager favoritesManager;
    
    private ActivityAdapter adapter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupRecyclerView(view);
        setupButtons(view);
        loadFavorites();
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActivityAdapter(getContext(), favoritesManager, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.btnReservas).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_favorites_to_reservasFragment));
        
        view.findViewById(R.id.btnMisDatos).setOnClickListener(v ->
                Toast.makeText(getContext(), R.string.nav_perfil, Toast.LENGTH_SHORT).show());
    }

    private void loadFavorites() {
        apiService.getAllActivities().enqueue(new Callback<PaginatedActivitiesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedActivitiesResponse> call, @NonNull Response<PaginatedActivitiesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ActivityResponse> allActivities = response.body().getContent();
                    Set<String> favIds = favoritesManager.getFavoriteIds();
                    
                    List<ActivityResponse> favoriteActivities = new ArrayList<>();
                    for (ActivityResponse activity : allActivities) {
                        if (favIds.contains(String.valueOf(activity.getId()))) {
                            favoriteActivities.add(activity);
                        }
                    }
                    adapter.setActivities(favoriteActivities);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedActivitiesResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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