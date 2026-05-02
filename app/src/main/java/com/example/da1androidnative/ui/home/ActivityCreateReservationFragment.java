package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.da1androidnative.ui.util.ToastHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.AuthResponse;
import com.example.da1androidnative.data.model.LoginRequest;
import com.example.da1androidnative.data.model.ReservaDetalleResponse;
import com.example.da1androidnative.data.model.ReservaRequest;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.ui.auth.LoginFragment;

import org.w3c.dom.Text;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ActivityCreateReservationFragment extends Fragment {

    @Inject
    ApiService apiService;
    private long activityId;
    private long scheduleId;
    private String date;
    private String time;
    private int availableSpots;
    private double precioBase;
    private int currentParticipants = 1;
    private TextView selectedDateText;
    private TextView selectedTimeText;
    private ImageButton btnMinus;
    private ImageButton btnPlus;
    private TextView participantsCountText;
    private TextView availableSpotsInfoText;
    private TextView summaryParticipantsText;
    private TextView summaryPriceText;
    private Button btnConfirmarReserva;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_create_reservation, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            this.activityId = args.getLong("activityId", -1L);
            this.scheduleId = args.getLong("scheduleId", -1L);
            this.date = args.getString("date", "");
            this.time = args.getString("time", "");
            this.availableSpots = args.getInt("availableSpots", -1);
            this.precioBase = args.getDouble("precioBase", 0.0);
        }

        selectedDateText = view.findViewById(R.id.selectedDateText);
        selectedTimeText = view.findViewById(R.id.selectedTimeText);
        btnMinus = view.findViewById(R.id.btnMinus);
        btnPlus = view.findViewById(R.id.btnPlus);
        btnConfirmarReserva = view.findViewById(R.id.btnConfirmarReserva);
        participantsCountText = view.findViewById(R.id.participantsCountText);
        availableSpotsInfoText = view.findViewById(R.id.availableSpotsInfoText);
        summaryParticipantsText = view.findViewById(R.id.summaryParticipantsText);
        summaryPriceText = view.findViewById(R.id.summaryPriceText);

        selectedDateText.setText(date);
        selectedTimeText.setText(time);

        updateUI();
        setupButtons(view);

    }

    private void updateUI() {
        participantsCountText.setText(String.valueOf(currentParticipants));
        availableSpotsInfoText.setText(String.format("Máximo %d participantes disponibles", availableSpots));
        summaryParticipantsText.setText(String.valueOf(currentParticipants));
        summaryPriceText.setText(String.format("$%.2f", currentParticipants * precioBase));
    }

    private void setupButtons(View view) {
        btnMinus.setOnClickListener(v -> {
            if (currentParticipants > 1) {
                currentParticipants--;
                updateUI();
            }
        });
        btnPlus.setOnClickListener(v -> {
            if (currentParticipants < availableSpots) {
                currentParticipants++;
                updateUI();
            }
        });
        btnConfirmarReserva.setOnClickListener(v -> {
            crearReserva();
        });
    }

    private void crearReserva() {
        ReservaRequest reservaRequest = new ReservaRequest(activityId, scheduleId, currentParticipants);
        apiService.reserveActivity(reservaRequest).enqueue(new Callback<ReservaRequest>() {
            @Override
            public void onResponse(@NonNull Call<ReservaRequest> call, @NonNull Response<ReservaRequest> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ToastHelper.show(getContext(), "Reserva Creada con Exito!");

                    NavHostFragment.findNavController(ActivityCreateReservationFragment.this)
                            .navigate(R.id.action_createReservationFragment_to_reservasFragment);
                } else {
                    ToastHelper.show(getContext(), "Error: Error Creando la Reserva");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReservaRequest> call, @NonNull Throwable t) {
                ToastHelper.show(getContext(), "Error de red: " + t.getMessage());
            }
        });

    }
}

