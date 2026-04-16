package com.example.da1androidnative.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.TokenManager;
import com.example.da1androidnative.data.model.AuthResponse;
import com.example.da1androidnative.data.model.LoginRequest;
import com.example.da1androidnative.data.model.OtpChallengeResponse;
import com.example.da1androidnative.data.model.OtpPurpose;
import com.example.da1androidnative.data.model.OtpRequest;
import com.example.da1androidnative.data.network.ApiService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    @Inject ApiService apiService;
    @Inject
    TokenManager tokenManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText mailEdit = view.findViewById(R.id.emailEditText);
        EditText passEdit = view.findViewById(R.id.passwordEditText);
        Button loginBtn = view.findViewById(R.id.loginButton);
        Button otpLoginButton = view.findViewById(R.id.otpLoginButton);
        Button otpRecoveryButton = view.findViewById(R.id.otpRecoveryButton);
        Button registerBtn = view.findViewById(R.id.registerButton);

        loginBtn.setOnClickListener(v1 -> {
            String email = mailEdit.getText().toString().trim();
            String password = passEdit.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Completa los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            performLogin(email, password);
        });

        otpLoginButton.setOnClickListener(v -> requestOtp(mailEdit.getText().toString().trim(), OtpPurpose.LOGIN));
        otpRecoveryButton.setOnClickListener(v -> requestOtp(mailEdit.getText().toString().trim(), OtpPurpose.ACCESS_RECOVERY));

        registerBtn.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_login_to_register);
        });
    }

    private void performLogin(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        apiService.login(loginRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    
                    // Guardo el token usando TokenManager
                    tokenManager.saveToken(token);
                    Toast.makeText(getContext(), "Bienvenido!", Toast.LENGTH_SHORT).show();

                    // Navegar al Home
                    NavHostFragment.findNavController(LoginFragment.this)
                            .navigate(R.id.home_nav_graph);
                } else {
                    Toast.makeText(getContext(), "Error: Credenciales inválidas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void requestOtp(String email, OtpPurpose purpose) {
        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Ingresa un email para solicitar el codigo", Toast.LENGTH_SHORT).show();
            return;
        }

        OtpRequest request = new OtpRequest(email, purpose);
        apiService.requestOtp(request).enqueue(new Callback<OtpChallengeResponse>() {
            @Override
            public void onResponse(@NonNull Call<OtpChallengeResponse> call, @NonNull Response<OtpChallengeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Codigo generado. Revisa la terminal del backend.", Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(LoginFragment.this).navigate(
                            R.id.action_login_to_otpVerificationFragment,
                            OtpVerificationFragment.createArgs(email, purpose)
                    );
                } else if (response.code() == 404) {
                    Toast.makeText(getContext(), "No existe una cuenta registrada con ese email", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "No se pudo generar el codigo OTP", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OtpChallengeResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
