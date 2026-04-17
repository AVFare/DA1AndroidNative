package com.example.da1androidnative.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.TokenManager;
import com.example.da1androidnative.data.model.AuthResponse;
import com.example.da1androidnative.data.model.OtpChallengeResponse;
import com.example.da1androidnative.data.model.OtpPurpose;
import com.example.da1androidnative.data.model.OtpRequest;
import com.example.da1androidnative.data.model.OtpVerifyRequest;
import com.example.da1androidnative.data.network.ApiService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class OtpVerificationFragment extends Fragment {

    public static final String ARG_EMAIL = "email";
    public static final String ARG_PURPOSE = "purpose";

    public static Bundle createArgs(String email, OtpPurpose purpose) {
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_PURPOSE, purpose.name());
        return args;
    }

    private String email;
    private OtpPurpose purpose;

    @Inject
    ApiService apiService;

    @Inject
    TokenManager tokenManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_otp_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        readArguments();
        bindContent(view);
        setupActions(view);
    }

    private void readArguments() {
        Bundle args = getArguments();
        email = args != null ? args.getString(ARG_EMAIL, "") : "";

        String purposeValue = args != null ? args.getString(ARG_PURPOSE, OtpPurpose.LOGIN.name()) : OtpPurpose.LOGIN.name();
        try {
            purpose = OtpPurpose.valueOf(purposeValue);
        } catch (IllegalArgumentException exception) {
            purpose = OtpPurpose.LOGIN;
        }
    }

    private void bindContent(View view) {
        TextView titleText = view.findViewById(R.id.otpTitleText);
        TextView subtitleText = view.findViewById(R.id.otpSubtitleText);
        TextView emailText = view.findViewById(R.id.otpEmailText);
        TextView helperText = view.findViewById(R.id.otpHelperText);

        if (purpose == OtpPurpose.ACCESS_RECOVERY) {
            titleText.setText(R.string.otp_recovery_title);
            subtitleText.setText(R.string.otp_recovery_subtitle);
        } else {
            titleText.setText(R.string.otp_login_title);
            subtitleText.setText(R.string.otp_login_subtitle);
        }

        emailText.setText(getString(R.string.otp_email_label, email));
        helperText.setText(R.string.otp_helper_text);
    }

    private void setupActions(View view) {
        EditText codeEditText = view.findViewById(R.id.otpCodeEditText);
        Button verifyButton = view.findViewById(R.id.verifyOtpButton);
        Button resendButton = view.findViewById(R.id.resendOtpButton);

        verifyButton.setOnClickListener(v -> verifyOtp(codeEditText.getText().toString().trim()));
        resendButton.setOnClickListener(v -> resendOtp());
    }

    private void verifyOtp(String code) {
        if (code.length() != 6) {
            Toast.makeText(getContext(), "Ingresa un codigo de 6 digitos", Toast.LENGTH_SHORT).show();
            return;
        }

        OtpVerifyRequest request = new OtpVerifyRequest(email, code, purpose);
        apiService.verifyOtp(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tokenManager.saveToken(response.body().getToken());
                    Toast.makeText(getContext(), "Acceso verificado", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(OtpVerificationFragment.this)
                            .navigate(R.id.action_otpVerificationFragment_to_home_nav_graph);
                } else {
                    Toast.makeText(getContext(), "Codigo invalido o expirado", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resendOtp() {
        OtpRequest request = new OtpRequest(email, purpose);
        apiService.resendOtp(request).enqueue(new Callback<OtpChallengeResponse>() {
            @Override
            public void onResponse(@NonNull Call<OtpChallengeResponse> call, @NonNull Response<OtpChallengeResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Se genero un nuevo codigo. Revisa la terminal del backend.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "No se pudo reenviar el codigo", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OtpChallengeResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
