package com.example.da1androidnative.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.InitiateEmailChangeRequest;
import com.example.da1androidnative.data.network.ApiService;
import com.google.android.material.textfield.TextInputEditText;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ChangeEmailFragment extends Fragment {

    @Inject ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText etNewEmail = view.findViewById(R.id.etNewEmail);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        view.findViewById(R.id.btnSendCode).setOnClickListener(v -> {
            String newEmail = etNewEmail.getText() != null
                    ? etNewEmail.getText().toString().trim()
                    : "";

            if (newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                Toast.makeText(getContext(), "Ingresá un email válido", Toast.LENGTH_SHORT).show();
                return;
            }

            initiateEmailChange(newEmail);
        });
    }

    private void initiateEmailChange(String newEmail) {
        apiService.initiateEmailChange(new InitiateEmailChangeRequest(newEmail))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Bundle args = new Bundle();
                            args.putString("newEmail", newEmail);
                            NavHostFragment.findNavController(ChangeEmailFragment.this)
                                    .navigate(R.id.action_changeEmail_to_otpEmailChangeFragment, args);
                        } else if (response.code() == 409) {
                            Toast.makeText(getContext(), "Ese correo ya está registrado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "No se pudo enviar el código", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}