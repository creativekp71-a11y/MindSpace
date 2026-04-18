package com.example.onlineexamapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public final class AppNotificationHelper {

    public static final String CHANNEL_ID = "mindspace_updates";
    private static final String CHANNEL_NAME = "MindSpace updates";
    private static final String CHANNEL_DESCRIPTION = "Notifications for new posts and activity updates.";

    private AppNotificationHelper() {
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(CHANNEL_DESCRIPTION);
        notificationManager.createNotificationChannel(channel);
    }

    public static boolean hasPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void showNotification(
            Context context,
            int notificationId,
            String title,
            String message
    ) {
        showChatNotification(context, notificationId, title, message, null, null, null);
    }

    public static void showChatNotification(
            Context context,
            int notificationId,
            String title,
            String message,
            String chatId,
            String senderId,
            String senderName
    ) {
        ensureChannel(context);
        if (!hasPermission(context)) {
            return;
        }

        Intent intent;
        if (chatId != null && senderId != null) {
            intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", chatId);
            intent.putExtra("receiverId", senderId);
            intent.putExtra("receiverName", senderName != null ? senderName : title);
        } else {
            // Default to Notifications screen
            // Assuming MainHomeActivity handles EXTRA_OPEN_TAB or simply opens Dashboard
            intent = new Intent(context, MainHomeActivity.class);
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Set to High for popups
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}
