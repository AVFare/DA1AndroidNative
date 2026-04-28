package com.example.da1androidnative.ui.home;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.FavoritesManager;
import com.example.da1androidnative.data.local.TokenManager;
import com.example.da1androidnative.data.model.ActivityResponse;
import com.example.da1androidnative.data.model.PaginatedActivitiesResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.data.network.NetworkUtils;
import com.example.da1androidnative.ui.home.adapter.ActivityAdapter;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class HomeFragment extends Fragment implements ActivityAdapter.OnActivityClickListener {

    @Inject ApiService apiService;
    @Inject TokenManager tokenManager;
    @Inject FavoritesManager favoritesManager;

    private ActivityAdapter adapter;
    private TextView tvOfflineBanner;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvOfflineBanner = view.findViewById(R.id.tvOfflineBanner);

        setupRecyclerView(view);
        registerNetworkCallback();
        loadActivities();

        view.findViewById(R.id.btnReservas).setOnClickListener(v ->
            NavHostFragment.findNavController(this).navigate(R.id.action_home_to_reservas));

        view.findViewById(R.id.btnFavoritos).setOnClickListener(v ->
            NavHostFragment.findNavController(this).navigate(R.id.action_home_to_favorites));
            
        view.findViewById(R.id.btnHistorial).setOnClickListener(v ->
            NavHostFragment.findNavController(this).navigate(R.id.action_home_to_activityHistory));

        view.findViewById(R.id.btnCalificaciones).setOnClickListener(v ->
            NavHostFragment.findNavController(this).navigate(R.id.action_home_to_reviewsFragment));

        view.findViewById(R.id.btnMisDatos).setOnClickListener(v ->
                NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.action_home_to_profileFragment));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateOfflineBanner();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterNetworkCallback();
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.activitiesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActivityAdapter(getContext(), favoritesManager, this);
        recyclerView.setAdapter(adapter);
    }

    private void registerNetworkCallback() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            updateOfflineBanner();
                            loadActivities();
                        });
                    }
                }

                @Override
                public void onLost(@NonNull Network network) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            updateOfflineBanner();
                        });
                    }
                }
            };
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        }
    }

    private void unregisterNetworkCallback() {
        if (networkCallback != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            }
        }
    }

    private void updateOfflineBanner() {
        if (tvOfflineBanner != null) {
            boolean isOffline = !NetworkUtils.isNetworkAvailable(getContext());
            tvOfflineBanner.setVisibility(isOffline ? View.VISIBLE : View.GONE);
        }
    }

    private void loadActivities() {
        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            // Podríamos cargar actividades cacheadas aquí si existieran
            return;
        }

        apiService.getAllActivities().enqueue(new Callback<PaginatedActivitiesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedActivitiesResponse> call, @NonNull Response<PaginatedActivitiesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setActivities(response.body().getContent());
                } else {
                    Toast.makeText(getContext(), "Error al obtener actividades", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedActivitiesResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityClick(Long activityId) {
        if (activityId == null) return;
        Bundle args = new Bundle();
        args.putLong("activityId", activityId);
        NavHostFragment.findNavController(this).navigate(R.id.action_home_to_activityDetalleFragment, args);
    }

    @Override
    public void onFavoriteClick(ActivityResponse activity) {
        favoritesManager.toggleFavorite(activity.getId());
        Toast.makeText(getContext(),
            favoritesManager.isFavorite(activity.getId()) ? "Agregado a favoritos" : "Quitado de favoritos",
            Toast.LENGTH_SHORT).show();
    }
}
