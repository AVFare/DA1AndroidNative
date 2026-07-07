package com.example.da1androidnative.data.network;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;

import javax.inject.Inject;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.Response;

@HiltWorker
public class NotificationPollingWorker extends Worker {
    private final String nombreTrabajoUnico = "notificationPolling";
    private final NotificationPollingClient pollingClient;

    @AssistedInject
    public NotificationPollingWorker(@Assisted @NonNull Context context, @Assisted @NonNull WorkerParameters parameters, NotificationPollingClient pollingClient) {
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
