package com.example.da1androidnative.data.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Singleton
public class NotificationPollingClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/api/v1/notifications";
    private final OkHttpClient client;
    private final Context context;

    @Inject
    public NotificationPollingClient(@ApplicationContext Context context, AuthInterceptor authInterceptor) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .readTimeout(65, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .build();
    }

    public Response executePoll() throws IOException {
        if (!isNetworkAvailable()) {
            throw new IOException("No network connectivity");
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/poll")
                .post(RequestBody.create("", null))
                .build();

        return client.newCall(request).execute();
    }

    public void sendAck(long notificationId) {
        if (!isNetworkAvailable()) return;

        Request request = new Request.Builder()
                .url(BASE_URL + "/" + notificationId + "/ack")
                .post(RequestBody.create("", null))
                .build();

        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
            } catch (IOException ignored) {
            }
        }).start();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        android.net.Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}