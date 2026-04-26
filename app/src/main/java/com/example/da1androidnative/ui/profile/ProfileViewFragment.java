package com.example.da1androidnative.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.databinding.FragmentProfileViewBinding;
import com.google.android.material.chip.Chip;

import java.util.HashMap;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileViewFragment extends Fragment {

    private FragmentProfileViewBinding binding;
    private ProfileViewModel viewModel;

    private static final Map<String, Integer> PREFERENCE_COLORS = new HashMap<String, Integer>() {{
        put("AVENTURA", 0xFFFF6D00);
        put("CULTURA", 0xFF7B1FA2);
        put("GASTRONOMIA", 0xFF1565C0);
        put("NATURALEZA", 0xFF2E7D32);
        put("RELAX", 0xFFF9A825);
    }};

    private static final Map<String, String> PREFERENCE_LABELS = new HashMap<String, String>() {{
        put("AVENTURA", "Aventura");
        put("CULTURA", "Cultura");
        put("GASTRONOMIA", "Gastronomía");
        put("NATURALEZA", "Naturaleza");
        put("RELAX", "Relax");
    }};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileViewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        observeViewModel();
        viewModel.loadProfile();

        binding.btnEditProfile.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileViewFragment_to_profileEditFragment));

        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnAccessSettings.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileViewFragment_to_accessSettingsFragment));
    }

    private void observeViewModel() {
        viewModel.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile == null) return;

            binding.tvFirstName.setText(profile.getFirstName());
            binding.tvLastName.setText(profile.getLastName());
            binding.tvEmail.setText(profile.getEmail());
            binding.tvPhone.setText(profile.getPhone() != null ? profile.getPhone() : "-");
            binding.tvReservedCountView.setText(String.valueOf(profile.getReservedActivitiesCount()));
            binding.tvCompletedCountView.setText(String.valueOf(profile.getCompletedActivitiesCount()));

            if (profile.getProfilePhoto() != null && !profile.getProfilePhoto().isEmpty()) {
                loadBase64Image(profile.getProfilePhoto());
            }

            binding.chipGroupPreferences.removeAllViews();
            if (profile.getPreferences() != null) {
                for (String pref : profile.getPreferences()) {
                    Chip chip = new Chip(requireContext());
                    chip.setText(PREFERENCE_LABELS.containsKey(pref) ? PREFERENCE_LABELS.get(pref) : pref);
                    chip.setClickable(false);
                    chip.setChipBackgroundColor(
                            android.content.res.ColorStateList.valueOf(
                                    PREFERENCE_COLORS.containsKey(pref) ? PREFERENCE_COLORS.get(pref) : 0xFF9E9E9E));
                    chip.setTextColor(0xFFFFFFFF);
                    binding.chipGroupPreferences.addView(chip);
                }
            }
        });
    }

    private void loadBase64Image(String base64) {
        try {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            binding.ivProfilePhotoView.setImageBitmap(bitmap);
        } catch (Exception e) {
            // queda la silueta por defecto
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}