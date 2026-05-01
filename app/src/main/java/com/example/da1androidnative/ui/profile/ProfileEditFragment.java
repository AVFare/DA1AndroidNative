package com.example.da1androidnative.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.UpdateUserProfileRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileEditFragment extends Fragment {

    private ProfileViewModel viewModel;
    private String currentPhotoBase64 = null;
    private ImageView ivProfilePhoto;
    private TextInputEditText etFirstName;
    private TextInputEditText etLastName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private CheckBox cbAventura;
    private CheckBox cbCultura;
    private CheckBox cbGastronomia;
    private CheckBox cbNaturaleza;
    private CheckBox cbRelax;
    private MaterialButton btnChangePhoto;
    private MaterialButton btnSaveProfile;
    private MaterialButton btnCancelEdit;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            handleImageSelected(uri);
                        }
                    }
            );

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        initViews(view);
        observeViewModel();
        setupListeners();
        viewModel.loadProfile();
    }

    private void initViews(View view) {
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        cbAventura = view.findViewById(R.id.cbAventura);
        cbCultura = view.findViewById(R.id.cbCultura);
        cbGastronomia = view.findViewById(R.id.cbGastronomia);
        cbNaturaleza = view.findViewById(R.id.cbNaturaleza);
        cbRelax = view.findViewById(R.id.cbRelax);
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnCancelEdit = view.findViewById(R.id.btnCancelEdit);
    }

    private void observeViewModel() {
        viewModel.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile == null) return;

            etFirstName.setText(profile.getFirstName());
            etLastName.setText(profile.getLastName());
            etEmail.setText(profile.getEmail());
            etPhone.setText(profile.getPhone());

            // Cambiado: usa profilePhoto (base64) en lugar de profilePictureUrl
            if (profile.getProfilePhoto() != null
                    && !profile.getProfilePhoto().isEmpty()) {
                currentPhotoBase64 = profile.getProfilePhoto();
                loadBase64Image(profile.getProfilePhoto());
            }

            if (profile.getTravelPreferences() != null) {
                cbAventura.setChecked(
                        profile.getTravelPreferences().contains("AVENTURA")
                );
                cbCultura.setChecked(
                        profile.getTravelPreferences().contains("CULTURA")
                );
                cbGastronomia.setChecked(
                        profile.getTravelPreferences().contains("GASTRONOMIA")
                );
                cbNaturaleza.setChecked(
                        profile.getTravelPreferences().contains("NATURALEZA")
                );
                cbRelax.setChecked(
                        profile.getTravelPreferences().contains("RELAX")
                );
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnSaveProfile.setEnabled(!Boolean.TRUE.equals(isLoading));
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(
                        requireContext(),
                        error,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(
                        requireContext(),
                        "Perfil actualizado",
                        Toast.LENGTH_SHORT
                ).show();

                NavHostFragment
                        .findNavController(ProfileEditFragment.this)
                        .navigateUp();
            }
        });
    }

    private void setupListeners() {
        btnChangePhoto.setOnClickListener(
                v -> pickImageLauncher.launch("image/*")
        );

        btnSaveProfile.setOnClickListener(v -> {
            String firstName = etFirstName.getText() != null
                    ? etFirstName.getText().toString().trim()
                    : "";

            String lastName = etLastName.getText() != null
                    ? etLastName.getText().toString().trim()
                    : "";

            String phone = etPhone.getText() != null
                    ? etPhone.getText().toString().trim()
                    : "";

            List<String> preferences = new ArrayList<>();

            if (cbAventura.isChecked()) preferences.add("AVENTURA");
            if (cbCultura.isChecked()) preferences.add("CULTURA");
            if (cbGastronomia.isChecked()) preferences.add("GASTRONOMIA");
            if (cbNaturaleza.isChecked()) preferences.add("NATURALEZA");
            if (cbRelax.isChecked()) preferences.add("RELAX");

            UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                    firstName,
                    lastName,
                    phone,
                    currentPhotoBase64,
                    preferences
            );

            viewModel.updateProfile(request);
        });

        btnCancelEdit.setOnClickListener(v ->
                NavHostFragment
                        .findNavController(ProfileEditFragment.this)
                        .navigateUp()
        );
    }

    private void handleImageSelected(Uri uri) {
        try {
            InputStream inputStream = requireContext()
                    .getContentResolver()
                    .openInputStream(uri);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);

            byte[] imageBytes = baos.toByteArray();

            currentPhotoBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            ivProfilePhoto.setImageBitmap(bitmap);

        } catch (IOException e) {
            Toast.makeText(
                    requireContext(),
                    "Error al cargar la imagen",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void loadBase64Image(String base64) {
        try {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);

            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    decodedBytes,
                    0,
                    decodedBytes.length
            );

            ivProfilePhoto.setImageBitmap(bitmap);

        } catch (Exception e) {
            // Si falla, queda el ícono por defecto
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
