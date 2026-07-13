package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.NotificationStorage;
import com.example.da1androidnative.data.model.Notification;
import com.example.da1androidnative.data.network.ApiService;
import com.example.da1androidnative.ui.home.adapter.NotificationAdapter;
import com.example.da1androidnative.ui.util.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class NotificationsFragment extends Fragment {

    @Inject ApiService apiService;
    @Inject NotificationStorage notificationStorage;
    
    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvNotifications = view.findViewById(R.id.rvNotifications);
        tvEmptyState = view.findViewById(R.id.tvEmptyNotifications);
        
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new NotificationAdapter(new ArrayList<>(), this::handleMarkAsRead);
        rvNotifications.setAdapter(adapter);

        updateUI();
        syncWithServer();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar por si el Worker guardó algo mientras la pantalla estaba en segundo plano
        updateUI();
    }

    private void updateUI() {
        List<Notification> saved = notificationStorage.getNotifications();
        adapter.setNotifications(saved);
        
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(saved.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void syncWithServer() {
        // Consultar al servidor por si hay notificaciones que el Worker aún no procesó
        apiService.getPendingNotifications().enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> serverNotifs = response.body();
                    boolean updated = false;
                    for (Notification n : serverNotifs) {
                        boolean isNew = notificationStorage.saveNotification(n);
                        if (isNew) {
                            // notificaciones de inmediato
                            NotificationHelper.mostrar(requireContext(), n);
                            updated = true;}
                    }
                    if (updated) updateUI();
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                // Error silencioso aquí, ya mostramos lo local
            }
        });
    }

    private void handleMarkAsRead(Notification novedad) {
        notificationStorage.removeNotification(novedad.getId());
        adapter.removeNotification(novedad);
        
        if (adapter.getItemCount() == 0 && tvEmptyState != null) {
            tvEmptyState.setVisibility(View.VISIBLE);
        }

        apiService.markAsRead(novedad.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Notificación marcada como leída", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }
}
