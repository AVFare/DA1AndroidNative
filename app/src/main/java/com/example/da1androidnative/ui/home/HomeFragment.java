package com.example.da1androidnative.ui.home;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricManager.Authenticators;
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
import com.google.android.material.switchmaterial.SwitchMaterial;

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
    private SwitchMaterial biometricSwitch;
    private ConnectivityManager.NetworkCallback networkCallback;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableBiometricIfPossible();
                } else {
                    Toast.makeText(getContext(), R.string.biometric_permission_denied, Toast.LENGTH_SHORT).show();
                    biometricSwitch.setChecked(false);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvOfflineBanner = view.findViewById(R.id.tvOfflineBanner);
        biometricSwitch = view.findViewById(R.id.biometricSwitch);

        setupRecyclerView(view);
        setupBiometricSwitch();
        registerNetworkCallback();
        loadActivities();
        setupNavigationButtons(view);
    }

    private void setupNavigationButtons(View view) {
        view.findViewById(R.id.btnReservas).setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_home_to_reservas));
        view.findViewById(R.id.btnFavoritos).setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_home_to_favorites));
        view.findViewById(R.id.btnHistorial).setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_home_to_activityHistory));
        view.findViewById(R.id.btnCalificaciones).setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_home_to_reviewsFragment));
        view.findViewById(R.id.btnMisDatos).setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_home_to_profileFragment));
        view.findViewById(R.id.btnNoticias).setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_home_to_news));
    }

    private void setupBiometricSwitch() {
        if (biometricSwitch == null) return;
        biometricSwitch.setChecked(tokenManager.isBiometricEnabled());
        biometricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (tokenManager.getSavedPassword() == null) {
                    Toast.makeText(getContext(), "Inicia sesión con contraseña una vez para habilitar biometría", Toast.LENGTH_LONG).show();
                    biometricSwitch.setChecked(false);
                    return;
                }
                String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ? Manifest.permission.USE_BIOMETRIC : Manifest.permission.USE_FINGERPRINT;
                requestPermissionLauncher.launch(permission);
            } else {
                tokenManager.setBiometricEnabled(false);
            }
        });
    }

    private void enableBiometricIfPossible() {
        BiometricManager manager = BiometricManager.from(requireContext());
        if (manager.canAuthenticate(Authenticators.BIOMETRIC_STRONG | Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
            tokenManager.setBiometricEnabled(true);
            Toast.makeText(getContext(), R.string.biometric_enabled_msg, Toast.LENGTH_SHORT).show();
        } else {
            biometricSwitch.setChecked(false);
            Toast.makeText(getContext(), R.string.biometric_error_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.activitiesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActivityAdapter(getContext(), favoritesManager, this);
        recyclerView.setAdapter(adapter);
    }

    private void registerNetworkCallback() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> { updateOfflineBanner(); loadActivities(); });
                }
                @Override
                public void onLost(@NonNull Network network) {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> updateOfflineBanner());
                }
            };
            cm.registerDefaultNetworkCallback(networkCallback);
        }
    }

    private void unregisterNetworkCallback() {
        if (networkCallback != null) {
            ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) cm.unregisterNetworkCallback(networkCallback);
        }
    }

    private void updateOfflineBanner() {
        if (tvOfflineBanner != null) {
            tvOfflineBanner.setVisibility(NetworkUtils.isNetworkAvailable(getContext()) ? View.GONE : View.VISIBLE);
        }
    }

    private void loadActivities() {
        if (!NetworkUtils.isNetworkAvailable(getContext())) return;
        apiService.getAllActivities().enqueue(new Callback<PaginatedActivitiesResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedActivitiesResponse> call, @NonNull Response<PaginatedActivitiesResponse> response) {
                if (response.isSuccessful() && response.body() != null) adapter.setActivities(response.body().getContent());
            }
            @Override
            public void onFailure(@NonNull Call<PaginatedActivitiesResponse> call, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(getContext(), "Fallo al cargar actividades", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); unregisterNetworkCallback(); }

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
        Toast.makeText(getContext(), favoritesManager.isFavorite(activity.getId()) ? "Agregado a favoritos" : "Quitado de favoritos", Toast.LENGTH_SHORT).show();
    }
}
