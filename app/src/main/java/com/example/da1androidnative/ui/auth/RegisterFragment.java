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
import com.example.da1androidnative.data.model.RegisterRequest;
import com.example.da1androidnative.data.network.ApiService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    @Inject ApiService apiService;
    @Inject TokenManager tokenManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText nameEdit = view.findViewById(R.id.nameEditText);
        EditText emailEdit = view.findViewById(R.id.emailEditText);
        EditText passEdit = view.findViewById(R.id.passwordEditText);
        EditText confirmPassEdit = view.findViewById(R.id.confirmPasswordEditText);
        EditText lastNameEdit = view.findViewById(R.id.lastNameEditText);
        EditText phoneEdit = view.findViewById(R.id.phoneEditText);
        Button registerBtn = view.findViewById(R.id.registerButton);

        registerBtn.setOnClickListener(view2 -> {
            String name = nameEdit.getText().toString().trim();
            String email = emailEdit.getText().toString().trim();
            String pass = passEdit.getText().toString().trim();
            String confirm = confirmPassEdit.getText().toString();
            String lastName = lastNameEdit.getText().toString().trim();
            String phone = phoneEdit.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirm)) {
                Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            RegisterRequest request = new RegisterRequest(email, pass, name, lastName, phone);
            performRegister(request);
        });
    }

    public void performRegister(RegisterRequest request) {
        apiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tokenManager.saveToken(response.body().getToken());
                    Toast.makeText(getContext(), "Cuenta creada con éxito", Toast.LENGTH_SHORT).show();

                    // Ahora NavHostFragment debería ser reconocido por el import
                    NavHostFragment.findNavController(RegisterFragment.this)
                            .navigate(R.id.home_nav_graph);
                } else {
                    Toast.makeText(getContext(), "Error al registrar: ¿El email ya existe?", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
