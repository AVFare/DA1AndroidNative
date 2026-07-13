package com.example.da1androidnative.data.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;

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
    private final OkHttpClient client;
    private final Context context;

    @Inject
    public NotificationPollingClient(@ApplicationContext Context context, AuthInterceptor authInterceptor) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .readTimeout(70, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .build();
    }

    private String getBaseUrl() {
        if (Build.FINGERPRINT.contains("generic")
                || Build.FINGERPRINT.contains("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT)
                || Build.PRODUCT.contains("sdk")) {

            return "http://10.0.2.2:8080/api/v1/notifications";
        } else {
            return "http://192.168.1.2:8080/api/v1/notifications";
        }
    }

    public Response executePoll() throws IOException {
        if (!isNetworkAvailable()) {
            throw new IOException("No network connectivity");
        }

        Request request = new Request.Builder()
                .url(getBaseUrl() + "/poll")
                .post(RequestBody.create("", null))
                .build();

        return client.newCall(request).execute();
    }

    public void sendAck(long notificationId) {
        if (!isNetworkAvailable()) return;

        Request request = new Request.Builder()
                .url(getBaseUrl() + "/" + notificationId + "/ack")
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
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }
}
