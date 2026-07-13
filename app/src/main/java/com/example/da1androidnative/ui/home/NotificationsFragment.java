package com.example.da1androidnative.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.local.NotificationStorage;
import com.example.da1androidnative.data.local.TokenManager;
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
    @Inject TokenManager tokenManager;
    
    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private View emptyStateContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        Toolbar toolbar = view.findViewById(R.id.toolbarNotifications);
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        rvNotifications = view.findViewById(R.id.rvNotifications);
        emptyStateContainer = view.findViewById(R.id.tvEmptyNotifications);
        
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
        long userId = tokenManager.getUserId();
        if (userId == -1) return;

        List<Notification> saved = notificationStorage.getNotifications(userId);
        adapter.setNotifications(saved);
        
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(saved.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void syncWithServer() {
        long userId = tokenManager.getUserId();
        if (userId == -1) return;

        // Consultar al servidor por si hay notificaciones que el Worker aún no procesó
        apiService.getPendingNotifications().enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> serverNotifs = response.body();
                    boolean updated = false;
                    for (Notification n : serverNotifs) {
                        boolean isNew = notificationStorage.saveNotification(userId, n);
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
        long userId = tokenManager.getUserId();
        if (userId == -1) return;

        notificationStorage.removeNotification(userId, novedad.getId());
        adapter.removeNotification(novedad);
        
        if (adapter.getItemCount() == 0 && emptyStateContainer != null) {
            emptyStateContainer.setVisibility(View.VISIBLE);
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