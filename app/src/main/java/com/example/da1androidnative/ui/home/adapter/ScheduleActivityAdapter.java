package com.example.da1androidnative.ui.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.ReservaResponse;
import com.example.da1androidnative.data.model.ScheduleResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivityAdapter extends RecyclerView.Adapter<ScheduleActivityAdapter.ScheduleViewHolder> {

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {

        TextView scheduleDateText;
        TextView scheduleTimeText;
        TextView availableSpotsText;
        MaterialCardView cardView;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            scheduleDateText = itemView.findViewById(R.id.scheduleDateText);
            scheduleTimeText = itemView.findViewById(R.id.scheduleTimeText);
            availableSpotsText = itemView.findViewById(R.id.availableSpotsText);
            cardView = itemView.findViewById(R.id.scheduleCard);
        }

    }

    private List<ScheduleResponse> horarios = new ArrayList<>();

    private final Context context;

    private ScheduleActivityAdapter.OnScheduleClickListener listener;

    public interface OnScheduleClickListener {
        void onScheduleClick(long scheduleId, String date, String time, int availableSpots);

    }

    public ScheduleActivityAdapter(Context context, ScheduleActivityAdapter.OnScheduleClickListener listener) {
        this.context = context;
        this.listener = listener;
    }
    public void setSchedules(List<ScheduleResponse> horarios) {
        this.horarios = horarios;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return horarios.size();
    }

    @NonNull
    @Override
    public ScheduleActivityAdapter.ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleActivityAdapter.ScheduleViewHolder(view);

    }

    @Override
    public void onBindViewHolder(ScheduleActivityAdapter.ScheduleViewHolder holder, int position) {

        ScheduleResponse horario = horarios.get(position);

        holder.scheduleDateText.setText(horario.getDate().toString());
        holder.scheduleTimeText.setText(horario.getTime().toString());
        holder.availableSpotsText.setText(String.format("Lugares Disponibles: %d", horario.getAvailableSpots()));

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onScheduleClick(horario.getScheduleId(), horario.getDate(), horario.getTime(), horario.getAvailableSpots());
            }
        });
    }
}
