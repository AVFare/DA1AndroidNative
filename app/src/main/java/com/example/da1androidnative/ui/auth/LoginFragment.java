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
import androidx.biometric.BiometricManager.Authenticators;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
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

import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    @Inject ApiService apiService;
    @Inject TokenManager tokenManager;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

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

        loginBtn.setOnClickListener(v1 -> {
            String email = mailEdit.getText().toString().trim();
            String password = passEdit.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), R.string.invalid_username, Toast.LENGTH_SHORT).show();
                return;
            }
            performLogin(email, password);
        });

        view.findViewById(R.id.otpLoginButton).setOnClickListener(v -> requestOtp(mailEdit.getText().toString().trim(), OtpPurpose.LOGIN));
        view.findViewById(R.id.registerButton).setOnClickListener(v -> 
            NavHostFragment.findNavController(this).navigate(R.id.action_login_to_register));

        setupBiometrics();
        
        if (tokenManager.isBiometricEnabled()) {
            biometricPrompt.authenticate(promptInfo);
        }
    }

    private void setupBiometrics() {
        executor = ContextCompat.getMainExecutor(requireContext());
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                String savedEmail = tokenManager.getSavedEmail();
                String savedPass = tokenManager.getSavedPassword();
                
                if (savedEmail != null && savedPass != null) {
                    performLogin(savedEmail, savedPass);
                } else {
                    Toast.makeText(getContext(), R.string.biometric_credentials_not_found, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_prompt_title))
                .setSubtitle(getString(R.string.biometric_prompt_subtitle))
                .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG | Authenticators.DEVICE_CREDENTIAL)
                .build();
    }

    private void performLogin(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        apiService.login(loginRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tokenManager.saveToken(response.body().getToken());
                    tokenManager.saveCredentials(email, password);
                    Toast.makeText(getContext(), R.string.welcome, Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_login_to_home);
                } else {
                    Toast.makeText(getContext(), "Error: Credenciales invalidas", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getContext(), "Ingresa un email", Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.requestOtp(new OtpRequest(email, purpose)).enqueue(new Callback<OtpChallengeResponse>() {
            @Override
            public void onResponse(@NonNull Call<OtpChallengeResponse> call, @NonNull Response<OtpChallengeResponse> response) {
                if (response.isSuccessful()) {
                    NavHostFragment.findNavController(LoginFragment.this).navigate(
                            R.id.action_login_to_otpVerificationFragment,
                            OtpVerificationFragment.createArgs(email, purpose)
                    );
                }
            }
            @Override
            public void onFailure(@NonNull Call<OtpChallengeResponse> call, @NonNull Throwable t) {}
        });
    }
}
