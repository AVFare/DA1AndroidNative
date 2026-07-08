package com.example.da1androidnative.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.da1androidnative.ui.util.ToastHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.TokenManager;
import com.example.da1androidnative.data.model.ConfirmEmailChangeRequest;
import com.example.da1androidnative.data.model.InitiateEmailChangeRequest;
import com.example.da1androidnative.data.network.ApiService;
import com.google.android.material.textfield.TextInputEditText;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class OtpEmailChangeFragment extends Fragment {

    @Inject ApiService apiService;

    @Inject
    TokenManager tokenManager;

    private String newEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_otp_email_change, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        newEmail = args != null ? args.getString("newEmail", "") : "";

        TextView tvEmailTarget = view.findViewById(R.id.tvEmailTarget);
        tvEmailTarget.setText("Se envió un código a: " + newEmail);

        TextInputEditText etOtpCode = view.findViewById(R.id.etOtpCode);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        view.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            String code = etOtpCode.getText() != null
                    ? etOtpCode.getText().toString().trim()
                    : "";

            if (code.length() != 6) {
                ToastHelper.show(getContext(), "Ingresá un código de 6 dígitos");
                return;
            }

            confirmEmailChange(code);
        });

        view.findViewById(R.id.btnResend).setOnClickListener(v -> resendCode());
    }


    private void confirmEmailChange(String code) {
        apiService.confirmEmailChange(new ConfirmEmailChangeRequest(newEmail, code))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            ToastHelper.show(getContext(), "Correo actualizado. Iniciá sesión con tu nuevo correo.");
                            tokenManager.clearToken();
                            tokenManager.clearCredentials();
                            NavHostFragment.findNavController(OtpEmailChangeFragment.this)
                                    .navigate(R.id.action_global_to_auth);
                        } else if (response.code() == 400) {
                            ToastHelper.show(getContext(), "Código inválido o expirado");
                        } else if (response.code() == 409) {
                            ToastHelper.show(getContext(), "Ese correo ya está registrado");
                        } else {
                            ToastHelper.show(getContext(), "No se pudo actualizar el correo");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        ToastHelper.show(getContext(), "Error de red: " + t.getMessage());
                    }
                });
    }

    private void resendCode() {
        apiService.initiateEmailChange(new InitiateEmailChangeRequest(newEmail))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            ToastHelper.show(getContext(), "Se reenvió el código. Revisá tu email.");
                        } else {
                            ToastHelper.show(getContext(), "No se pudo reenviar el código");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        ToastHelper.show(getContext(), "Error de red: " + t.getMessage());
                    }
                });
    }
}
