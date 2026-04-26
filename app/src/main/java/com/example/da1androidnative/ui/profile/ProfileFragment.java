package com.example.da1androidnative.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.UpdateUserProfileRequest;
import com.example.da1androidnative.databinding.FragmentProfileBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private String currentPhotoBase64 = null;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    handleImageSelected(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        observeViewModel();
        setupListeners();
        viewModel.loadProfile();
    }

    private void observeViewModel() {
        viewModel.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile == null) return;

            binding.etFirstName.setText(profile.getFirstName());
            binding.etLastName.setText(profile.getLastName());
            binding.etEmail.setText(profile.getEmail());
            binding.etPhone.setText(profile.getPhone());
            binding.tvReservedCount.setText(String.valueOf(profile.getReservedActivitiesCount()));
            binding.tvCompletedCount.setText(String.valueOf(profile.getCompletedActivitiesCount()));

            if (profile.getProfilePhoto() != null && !profile.getProfilePhoto().isEmpty()) {
                currentPhotoBase64 = profile.getProfilePhoto();
                loadBase64Image(profile.getProfilePhoto());
            }

            if (profile.getPreferences() != null) {
                binding.cbAventura.setChecked(profile.getPreferences().contains("AVENTURA"));
                binding.cbCultura.setChecked(profile.getPreferences().contains("CULTURA"));
                binding.cbGastronomia.setChecked(profile.getPreferences().contains("GASTRONOMIA"));
                binding.cbNaturaleza.setChecked(profile.getPreferences().contains("NATURALEZA"));
                binding.cbRelax.setChecked(profile.getPreferences().contains("RELAX"));
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnSaveProfile.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        binding.btnChangePhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        binding.btnSaveProfile.setOnClickListener(v -> {
            String firstName = binding.etFirstName.getText() != null
                    ? binding.etFirstName.getText().toString().trim() : "";
            String lastName = binding.etLastName.getText() != null
                    ? binding.etLastName.getText().toString().trim() : "";
            String phone = binding.etPhone.getText() != null
                    ? binding.etPhone.getText().toString().trim() : "";

            List<String> preferences = new ArrayList<>();
            if (binding.cbAventura.isChecked()) preferences.add("AVENTURA");
            if (binding.cbCultura.isChecked()) preferences.add("CULTURA");
            if (binding.cbGastronomia.isChecked()) preferences.add("GASTRONOMIA");
            if (binding.cbNaturaleza.isChecked()) preferences.add("NATURALEZA");
            if (binding.cbRelax.isChecked()) preferences.add("RELAX");

            UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                    firstName, lastName, phone, currentPhotoBase64, preferences);

            viewModel.updateProfile(request);
        });
    }

    private void handleImageSelected(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            currentPhotoBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            binding.ivProfilePhoto.setImageBitmap(bitmap);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBase64Image(String base64) {
        try {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            binding.ivProfilePhoto.setImageBitmap(bitmap);
        } catch (Exception e) {
            // Si falla, queda el ícono por defecto
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}