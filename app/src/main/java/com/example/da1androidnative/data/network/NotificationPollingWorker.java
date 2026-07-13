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

    @AssistedInject
    public NotificationPollingWorker(@Assisted @NonNull Context context, @Assisted @NonNull WorkerParameters parameters, 
                                   NotificationPollingClient pollingClient,
                                   NotificationStorage notificationStorage) {
        super(context, parameters);
        this.pollingClient = pollingClient;
        this.notificationStorage = notificationStorage;
        this.gson = new Gson();
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
        //try-with-resources basicamente cierra el recurso al terminar el try y no se pisan las conexiones.
        try (Response response = pollingClient.executePoll()) {

            if (response.isSuccessful()) {
                if (response.code() == 200 && response.body() != null) {
                    String json = response.body().string();
                    Type listType = new TypeToken<List<Notification>>() {}.getType();
                    List<Notification> novedades = gson.fromJson(json, listType);

                    if (novedades != null && !novedades.isEmpty()) {
                        for (Notification n : novedades) {
                            // guarda localmente
                            notificationStorage.saveNotification(n);
                            // pop up
                            NotificationHelper.mostrar(getApplicationContext(), n);
                            pollingClient.sendAck(n.getId());
                        }
                    }
                }
                enqueueNextPoll();
                return Result.success();
            }

            if (response.code() == 401) {
                return Result.failure();
            }

            return Result.retry();

        } catch (IOException exception) {
            Log.e(TAG, "Worker: Error de red -> " + exception.getMessage());
            enqueueNextPoll();
            return Result.success();
        }
    }
}
