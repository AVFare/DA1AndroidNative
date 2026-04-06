package com.example.da1androidnative.ui.auth;

import android.os.Bundle;

import androidx.annotation.NavigationRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.da1androidnative.R;


public class LoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText mailEdit = view.findViewById(R.id.emailEditText);
        EditText passEdit = view.findViewById(R.id.passwordEditText);

        Button loginBtn = view.findViewById(R.id.loginButton);
        Button registerBtn = view.findViewById(R.id.registerButton);

        loginBtn.setOnClickListener( view1 -> {
            String username = mailEdit.getText().toString().trim();
            String password = passEdit.getText().toString().trim();

            Toast.makeText(getContext(), String.format("Hola Mundo %s, %s", username, password), Toast.LENGTH_SHORT).show();

            // TODO: Adicionar Logica de Logueo con Retrofit.

            Bundle args = new Bundle();

            // TODO: Agregar a args las propiedades que se consideren necesarias desde la API

        });

        // TODO: Agregar la logica del boton de registro.


    }
}