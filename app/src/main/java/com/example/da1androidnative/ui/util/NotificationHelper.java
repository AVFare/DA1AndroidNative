package com.example.da1androidnative.ui.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavDeepLinkBuilder;

import com.example.da1androidnative.R;
import com.example.da1androidnative.data.model.Notification;
import com.example.da1androidnative.ui.MainActivity;

public class NotificationHelper {
    public static final String CHANNEL_ID = "channel_novedades_xplore";
    private static final String TAG = "NOTIFICATION_HELPER";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones Criticas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Avisos inmediatos sobre cancelaciones y reprogramaciones.");
            channel.enableVibration(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void mostrar(Context context, Notification novedad) {
        createNotificationChannel(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
                .setComponentName(MainActivity.class)
                .setGraph(R.navigation.home_nav_graph)
                .setDestination(R.id.notificationsFragment)
                .createPendingIntent();

        String titulo;
        int color;
        String type = novedad.getType() != null ? novedad.getType().toUpperCase() : "INFO";
        String payload = novedad.getPayload() != null ? novedad.getPayload() : "Sin detalle";

        if (type.equals("CANCELLED") || (payload.contains("cancelad") && payload.contains("actividad"))){
            titulo = "⚠️ ACTIVIDAD CANCELADA";
            color = android.R.color.holo_red_dark;
        } else if (type.equals("RESCHEDULED") || payload.contains("reprogramada")) {
            titulo = "🕒 CAMBIO DE HORARIO";
            color = android.R.color.holo_orange_dark;

        }
        else if (type.equals("REMINDER") || payload.contains("mañana") || payload.contains("24 horas")) {
            // recordatorio de 24hs
            titulo = "📅 ¡TU ACTIVIDAD ES MAÑANA!";
            color = R.color.teal_primary;}
        else {
            titulo = "Aviso de XploreNow";
            color = R.color.teal_primary;
        }
        Log.d("DEBUG", "type recibido: " + novedad.getType());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(titulo)
                .setContentText(novedad.getPayload())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(android.app.Notification.DEFAULT_ALL)
                .setColor(ContextCompat.getColor(context, color))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(novedad.getId().intValue(), builder.build());
        } catch (SecurityException ignored) {}
    }
}
