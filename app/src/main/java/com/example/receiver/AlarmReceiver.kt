package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra("EVENT_ID", -1)
        val title = intent.getStringExtra("EVENT_TITLE") ?: "تذكير بالحدث"
        val desc = intent.getStringExtra("EVENT_DESC") ?: "تبدأ فعاليتك الآن"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "calendar_events_channel"

        // Create notification channel on Android Oreo+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "إشعارات التقويم",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة مخصصة للتذكير بالفعاليات والمهام المجدولة"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Open MainActivity when notification is clicked
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId.toInt(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Draw drawable fallback
        val iconRes = android.R.drawable.ic_lock_idle_alarm

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(eventId.toInt(), notification)
    }
}
