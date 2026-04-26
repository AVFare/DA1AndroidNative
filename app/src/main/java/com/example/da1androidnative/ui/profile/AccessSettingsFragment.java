package com.example.da1androidnative.ui.profile;

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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricManager.Authenticators;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.TokenManager;
import com.example.da1androidnative.data.network.ApiService;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class AccessSettingsFragment extends Fragment {

    @Inject TokenManager tokenManager;
    @Inject ApiService apiService;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_access_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        setupBiometricSwitch(view);

        view.findViewById(R.id.btnChangeEmail).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_accessSettings_to_changeEmailFragment));

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());

        view.findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> confirmDeleteAccount());
    }

    // — Biometría (movida de HomeFragment) —

    private void setupBiometricSwitch(View view) {
        biometricSwitch = view.findViewById(R.id.biometricSwitch);
        biometricSwitch.setChecked(tokenManager.isBiometricEnabled());

        biometricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                        ? Manifest.permission.USE_BIOMETRIC
                        : Manifest.permission.USE_FINGERPRINT;
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
                    Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                    enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            Authenticators.BIOMETRIC_STRONG);
                    startActivity(enrollIntent);
                }
                biometricSwitch.setChecked(false);
                break;
            default:
                biometricSwitch.setChecked(false);
        }
    }

    private void showBiometricPromptToEnable() {
        executor = ContextCompat.getMainExecutor(requireContext());
        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
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

    // — Cerrar sesión —

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Querés cerrar la sesión?")
                .setPositiveButton("Cerrar sesión", (dialog, which) -> performLogout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void performLogout() {
        tokenManager.clearToken();
        tokenManager.clearCredentials();
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_global_to_auth);
    }

    // — Eliminar cuenta —

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar cuenta")
                .setMessage("Esta acción es permanente. Se eliminarán todos tus datos y reservas. ¿Querés continuar?")
                .setPositiveButton("Eliminar", (dialog, which) -> performDeleteAccount())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void performDeleteAccount() {
        apiService.deleteAccount().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    tokenManager.clearToken();
                    tokenManager.clearCredentials();
                    tokenManager.setBiometricEnabled(false);
                    NavHostFragment.findNavController(AccessSettingsFragment.this)
                            .navigate(R.id.action_global_to_auth);
                } else {
                    Toast.makeText(getContext(), "No se pudo eliminar la cuenta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}