package com.example.da1androidnative.ui.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<Notification> notifications;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onMarkAsReadClick(Notification novedad);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification novedad = notifications.get(position);
        String type = novedad.getType() != null ? novedad.getType().toUpperCase() : "INFO";


        switch (type) {
            case "CANCELLED":
                holder.tvTitle.setText("ACTIVIDAD CANCELADA");
                holder.tvTitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
                holder.ivIcon.setImageResource(android.R.drawable.ic_delete);
                holder.ivIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
                break;
            case "RESCHEDULED":
                holder.tvTitle.setText("CAMBIO DE HORARIO");
                holder.tvTitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
                holder.ivIcon.setImageResource(android.R.drawable.ic_popup_reminder);
                holder.ivIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
                break;
            case "REMINDER":
                holder.tvTitle.setText("¡TU ACTIVIDAD ES MAÑANA!");
                holder.tvTitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.teal_primary));
                holder.ivIcon.setImageResource(android.R.drawable.ic_popup_reminder);
                holder.ivIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.teal_primary));
                break;
            default:
                holder.tvTitle.setText("Aviso de XploreNow");
                holder.tvTitle.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.teal_primary));
                holder.ivIcon.setImageResource(android.R.drawable.ic_dialog_info);
                holder.ivIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.teal_primary));
                break;
        }

        holder.tvPayload.setText(novedad.getPayload());
        
        String fecha = novedad.getDeliverAt();
        if (fecha != null && fecha.length() >= 16) {
            holder.tvDate.setText(fecha.replace("T", " ").substring(0, 16));
        } else {
            holder.tvDate.setText(fecha != null ? fecha : "Recién");
        }

        holder.btnMarkAsRead.setOnClickListener(v -> listener.onMarkAsReadClick(novedad));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setNotifications(List<Notification> newNotifications) {
        this.notifications.clear();
        this.notifications.addAll(newNotifications);
        notifyDataSetChanged();
    }

    public void removeNotification(Notification novedad) {
        int position = notifications.indexOf(novedad);
        if (position != -1) {
            notifications.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPayload, tvDate;
        ImageView ivIcon;
        Button btnMarkAsRead;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvPayload = itemView.findViewById(R.id.tvNotificationPayload);
            tvDate = itemView.findViewById(R.id.tvNotificationDate);
            ivIcon = itemView.findViewById(R.id.ivNotificationIcon);
            btnMarkAsRead = itemView.findViewById(R.id.btnMarkAsRead);
        }
    }
}
