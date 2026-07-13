package com.example.da1androidnative.data.network;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.da1androidnative.data.local.NotificationStorage;
import com.example.da1androidnative.data.local.TokenManager;
import com.example.da1androidnative.data.model.Notification;
import com.example.da1androidnative.ui.util.NotificationHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import okhttp3.Response;

@HiltWorker
public class NotificationPollingWorker extends Worker {
    private static final String TAG = "POLLING_WORKER";
    public static final String nombreTrabajoUnico = "notificationPolling";
    private final NotificationPollingClient pollingClient;
    private final NotificationStorage notificationStorage;
    private final Gson gson;

    private final TokenManager tokenManager;

    @AssistedInject
    public NotificationPollingWorker(@Assisted @NonNull Context context, @Assisted @NonNull WorkerParameters parameters,
                                     NotificationPollingClient pollingClient,
                                     NotificationStorage notificationStorage,
                                     TokenManager tokenManager) {
        super(context, parameters);
        this.pollingClient = pollingClient;
        this.notificationStorage = notificationStorage;
        this.gson = new Gson();
        this.tokenManager = tokenManager;
    }

    private void enqueueNextPoll() {
        OneTimeWorkRequest siguiente = new OneTimeWorkRequest.Builder(NotificationPollingWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .setInitialDelay(1, TimeUnit.SECONDS)
                .build();
        
        WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(
                nombreTrabajoUnico, 
                ExistingWorkPolicy.REPLACE, 
                siguiente
        );
    }

    @Override
    @NonNull
    public Result doWork() {
        try (Response response = pollingClient.executePoll()) {
            long userId= tokenManager.getUserId();
            if (response.isSuccessful()) {
                if (response.code() == 200 && response.body() != null) {
                    String json = response.body().string();
                    
                    // Usamos el modelo Notification unificado
                    Type listType = new TypeToken<List<Notification>>() {}.getType();
                    List<Notification> novedades = gson.fromJson(json, listType);

                    if (novedades != null && !novedades.isEmpty()) {
                        for (Notification n : novedades) {
                            // 1. Guardar localmente (esto ahora usa el UserID internamente)
                            boolean esNueva = notificationStorage.saveNotification(userId,n);
                            
                            if (esNueva) {
                                // 2. Mostrar pop-up solo si no existía para este usuario
                                NotificationHelper.mostrar(getApplicationContext(), n);
                            }
                            
                            // 3. Confirmar al servidor
                            pollingClient.sendAck(n.getId());
                        }
                    }
                }
                
                enqueueNextPoll();
                return Result.success();
            }

            if (response.code() == 401) return Result.failure();
            
            enqueueNextPoll();
            return Result.success();

        } catch (IOException exception) {
            enqueueNextPoll();
            return Result.success();
        }
    }
}
