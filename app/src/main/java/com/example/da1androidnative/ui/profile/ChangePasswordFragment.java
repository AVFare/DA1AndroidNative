package com.example.da1androidnative.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

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
    @Inject private TokenManager tokenManager;

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
                Toast.makeText(getContext(), "Completá todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(repeatedPassword)) {
                Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            int otp;
            try {
                otp = Integer.parseInt(otpText);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Ingresá un código OTP válido", Toast.LENGTH_SHORT).show();
                return;
            }

            confirmChangePassword(otp, newPassword);
        });
    }

    private void confirmChangePassword(int otp, String newPassword) {
        apiService.ConfirmChangePassword(new ConfirmChangePasswordRequest(otp, newPassword))
                .enqueue(new Callback<ConfirmChangePasswordResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ConfirmChangePasswordResponse> call,
                                           @NonNull Response<ConfirmChangePasswordResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Contraseña actualizada correctamente, Ingrese Nuevamente", Toast.LENGTH_SHORT).show();
                            performLogout();
                        } else {
                            Toast.makeText(getContext(), "No se pudo cambiar la contraseña, Intente de Nuevo Luego", Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(ChangePasswordFragment.this)
                                    .navigate(R.id.action_changePassword_to_accessSettingsFragment);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ConfirmChangePasswordResponse> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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