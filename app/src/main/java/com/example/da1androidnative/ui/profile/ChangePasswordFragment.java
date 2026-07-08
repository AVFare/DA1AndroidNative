package com.example.da1androidnative.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.da1androidnative.ui.util.ToastHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.work.WorkManager;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.TokenManager;
import com.example.da1androidnative.data.model.ConfirmChangePasswordRequest;
import com.example.da1androidnative.data.model.ConfirmChangePasswordResponse;
import com.example.da1androidnative.data.network.ApiService;
import com.google.android.material.textfield.TextInputEditText;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ChangePasswordFragment extends Fragment {

    @Inject ApiService apiService;
    private TextInputEditText password;
    private TextInputEditText confirmPassword;
    private TextInputEditText optCode;
    private Button confirmButton;
    @Inject TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        password = view.findViewById(R.id.newPasswordEditText);
        confirmPassword = view.findViewById(R.id.confirmPasswordEditText);
        optCode = view.findViewById(R.id.otpCodeEditText);
        confirmButton = view.findViewById(R.id.confirmChangePasswordButton);


        confirmButton.setOnClickListener(v -> {
            String newPassword = password.getText() != null ? password.getText().toString().trim() : "";
            String repeatedPassword = confirmPassword.getText() != null ? confirmPassword.getText().toString().trim() : "";
            String otpText = optCode.getText() != null ? optCode.getText().toString().trim() : "";

            if (newPassword.isEmpty() || repeatedPassword.isEmpty() || otpText.isEmpty()) {
                ToastHelper.show(getContext(), "Completá todos los campos");
                return;
            }

            if (newPassword.length() < 8) {
                ToastHelper.show(getContext(), "La contraseña debe tener al menos 8 caracteres");
                return;
            }

            if (!newPassword.equals(repeatedPassword)) {
                ToastHelper.show(getContext(), "Las contraseñas no coinciden");
                return;
            }

            confirmChangePassword(otpText, newPassword);
        });
    }

    private void confirmChangePassword(String code, String newPassword) {

        ConfirmChangePasswordRequest request = new ConfirmChangePasswordRequest(code, newPassword);

        apiService.confirmChangePassword(request).enqueue(new Callback<ConfirmChangePasswordResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ConfirmChangePasswordResponse> call,
                                           @NonNull Response<ConfirmChangePasswordResponse> response) {
                        if (response.isSuccessful()) {
                            ToastHelper.show(getContext(), "Contraseña actualizada correctamente, Ingrese Nuevamente");
                            performLogout();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                System.err.println("Error response: " + errorBody);
                            } catch (Exception e) {
                                ToastHelper.show(getContext(), "No se pudo cambiar la contraseña, Verifique el OTP");
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ConfirmChangePasswordResponse> call, @NonNull Throwable t) {
                        ToastHelper.show(getContext(), "Error de red: " + t.getMessage());
                        NavHostFragment.findNavController(ChangePasswordFragment.this)
                                .navigate(R.id.action_changePassword_to_accessSettingsFragment);
                    }
                });
    }

    private void performLogout() {
        tokenManager.clearToken();
        tokenManager.clearCredentials();
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_global_to_auth);
    }
}