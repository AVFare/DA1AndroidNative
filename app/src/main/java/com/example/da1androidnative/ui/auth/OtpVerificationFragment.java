package com.example.da1androidnative.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.OtpPurpose;

import dagger.hilt.android.AndroidEntryPoint;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_otp_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        readArguments();
        bindContent(view);
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
}
