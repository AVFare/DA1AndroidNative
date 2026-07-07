package com.example.da1androidnative.data.network;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

import okhttp3.Response;

public class NotificationPollingWorker extends Worker {
    private final String nombreTrabajoUnico = "notificationPolling";
    private final NotificationPollingClient pollingClient;

    public NotificationPollingWorker(@NonNull Context context, @NonNull WorkerParameters parameters, NotificationPollingClient pollingClient) {
        super(context, parameters);
        this.pollingClient = pollingClient;
    }

    private void enqueueNextPoll() {
        OneTimeWorkRequest siguiente = new OneTimeWorkRequest.Builder(NotificationPollingWorker.class).setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build();
        WorkManager.getInstance(getApplicationContext()).enqueueUniqueWork(nombreTrabajoUnico, ExistingWorkPolicy.REPLACE, siguiente);
    }

    @Override
    @NonNull
    public Result doWork() {
        try {
            Response response = pollingClient.executePoll();

            if (response.isSuccessful()) {
                //Jordi aca va lo tuyo
                //mostrarNotificacion(parametros);
                //TODO: Mostrar Notificacion

                enqueueNextPoll();
                return Result.success();
            }

            if (response.code() == 401) {
                return Result.failure();
            }

            return Result.retry();

        }
        catch (IOException exception) {
            return Result.retry();
        }
    }
}
