package com.example.da1androidnative.ui.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.da1androidnative.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.HashMap;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileViewFragment extends Fragment {

    private ProfileViewModel viewModel;
    private ImageView ivProfilePhotoView;
    private ImageButton btnBack;
    private TextView tvFirstName;
    private TextView tvLastName;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvReservedCountView;
    private TextView tvCompletedCountView;
    private ChipGroup chipGroupPreferences;
    private MaterialButton btnEditProfile;
    private View btnAccessSettings;
    private View layoutReservadasCard;
    private View layoutRealizadasCard;
    private LinearProgressIndicator progressIndicator;

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
        return inflater.inflate(R.layout.fragment_profile_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        initViews(view);
        observeViewModel();
        viewModel.loadProfile();

        btnEditProfile.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileViewFragment_to_profileEditFragment));

        btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        btnAccessSettings.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileViewFragment_to_accessSettingsFragment));

        layoutReservadasCard.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileViewFragment_to_reservasFragment));

        layoutRealizadasCard.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profileViewFragment_to_activityHistoryFragment));
        progressIndicator.setVisibility(View.VISIBLE);
        if (progressIndicator == null) {
            android.util.Log.e("DEBUG", "progressIndicator es NULL");
        } else {
            android.util.Log.e("DEBUG", "progressIndicator encontrado, forzando VISIBLE");
            progressIndicator.setVisibility(View.VISIBLE);
        }
    }

    private void initViews(View view) {
        ivProfilePhotoView = view.findViewById(R.id.ivProfilePhotoView);
        btnBack = view.findViewById(R.id.btnBack);
        tvFirstName = view.findViewById(R.id.tvFirstName);
        tvLastName = view.findViewById(R.id.tvLastName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvReservedCountView = view.findViewById(R.id.tvReservedCountView);
        tvCompletedCountView = view.findViewById(R.id.tvCompletedCountView);
        chipGroupPreferences = view.findViewById(R.id.chipGroupPreferences);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnAccessSettings = view.findViewById(R.id.btnAccessSettings);
        layoutReservadasCard = view.findViewById(R.id.layoutReservadasCard);
        layoutRealizadasCard = view.findViewById(R.id.layoutRealizadasCard);
        progressIndicator = view.findViewById(R.id.progressIndicator);
    }

    private void observeViewModel() {
        viewModel.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile == null) return;

            tvFirstName.setText(profile.getFirstName());
            tvLastName.setText(profile.getLastName());
            tvEmail.setText(profile.getEmail());
            tvPhone.setText(profile.getPhone() != null ? profile.getPhone() : "-");
            tvReservedCountView.setText(String.valueOf(profile.getReservedActivitiesCount()));
            tvCompletedCountView.setText(String.valueOf(profile.getCompletedActivitiesCount()));

            if (profile.getProfilePhoto() != null && !profile.getProfilePhoto().isEmpty()) {
                loadBase64Image(profile.getProfilePhoto());
            }

            chipGroupPreferences.removeAllViews();
            if (profile.getTravelPreferences() != null) {
                for (String pref : profile.getTravelPreferences()) {
                    Chip chip = new Chip(requireContext());
                    chip.setText(PREFERENCE_LABELS.containsKey(pref) ? PREFERENCE_LABELS.get(pref) : pref);
                    chip.setClickable(false);
                    chip.setChipBackgroundColor(
                            android.content.res.ColorStateList.valueOf(
                                    PREFERENCE_COLORS.containsKey(pref) ? PREFERENCE_COLORS.get(pref) : 0xFF9E9E9E));
                    chip.setTextColor(0xFFFFFFFF);
                    chipGroupPreferences.addView(chip);
                }
            }
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            progressIndicator.setVisibility(loading != null && loading ? View.VISIBLE : View.GONE);
        });
    }

    private void loadBase64Image(String base64) {
        try {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            ivProfilePhotoView.setImageBitmap(bitmap);
        } catch (Exception e) {
            // queda la silueta por defecto
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
