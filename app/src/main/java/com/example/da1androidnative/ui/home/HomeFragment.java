package com.example.da1androidnative.ui.home;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricManager.Authenticators;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
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
import com.example.da1androidnative.ui.home.adapter.ActivityAdapter;
import com.google.android.material.switchmaterial.SwitchMaterial;

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
    private SwitchMaterial biometricSwitch;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    checkBiometricHardware();
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
        setupRecyclerView(view);
        setupBiometricSwitch(view);
        loadActivities();
        
        view.findViewById(R.id.btnReservas).setOnClickListener(v -> 
            NavHostFragment.findNavController(this).navigate(R.id.action_home_to_reservas));
        
        view.findViewById(R.id.btnFavoritos).setOnClickListener(v ->
            NavHostFragment.findNavController(this).navigate(R.id.action_home_to_favorites));
            
        view.findViewById(R.id.btnMisDatos).setOnClickListener(v -> 
            Toast.makeText(getContext(), R.string.nav_perfil, Toast.LENGTH_SHORT).show());
    }

    private void setupBiometricSwitch(View view) {
        biometricSwitch = view.findViewById(R.id.biometricSwitch);
        biometricSwitch.setChecked(tokenManager.isBiometricEnabled());

        biometricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) 
                        ? Manifest.permission.USE_BIOMETRIC : Manifest.permission.USE_FINGERPRINT;
                requestPermissionLauncher.launch(permission);
            } else {
                tokenManager.setBiometricEnabled(false);
                tokenManager.clearCredentials();
                Toast.makeText(getContext(), R.string.biometric_disabled_msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkBiometricHardware() {
        BiometricManager manager = BiometricManager.from(requireContext());
        int canAuth = manager.canAuthenticate(Authenticators.BIOMETRIC_STRONG | Authenticators.DEVICE_CREDENTIAL);

        switch (canAuth) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                showBiometricPromptToEnable();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(getContext(), R.string.biometric_error_not_available, Toast.LENGTH_SHORT).show();
                biometricSwitch.setChecked(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                    enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, Authenticators.BIOMETRIC_STRONG);
                    startActivity(enrollIntent);
                }
                biometricSwitch.setChecked(false);
                break;
            default:
                biometricSwitch.setChecked(false);
                break;
        }
    }

    private void showBiometricPromptToEnable() {
        executor = ContextCompat.getMainExecutor(requireContext());
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                tokenManager.setBiometricEnabled(true);
                Toast.makeText(getContext(), R.string.biometric_enabled_msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                biometricSwitch.setChecked(false);
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_prompt_title))
                .setSubtitle(getString(R.string.biometric_prompt_subtitle))
                .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG | Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.activitiesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ActivityAdapter(getContext(), favoritesManager, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadActivities() {
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
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
