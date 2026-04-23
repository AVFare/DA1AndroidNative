package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.network.ApiService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ActivityDetalleFragment extends Fragment {

    @Inject
    ApiService apiService;

    private long activityId;

    private ImageView activityImageView;
    private TextView activityNameText;
    private TextView categoryBadgeText;
    private TextView destionationText;
    private TextView duratioText;
    private TextView priceText;
    private TextView fullDescriptionText;
    private TextView meetingPointText;
    private TextView guideNameText;
    private TextView languageText;
    private TextView inclusionsText;
    private TextView cancellationPolicyText;
    private Button btnReservar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_detalle, container, false);
    }
}

