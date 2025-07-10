package com.outdu.camconnect.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.outdu.camconnect.MainActivity
import com.outdu.camconnect.R

object NotificationHelper {
    private const val CHANNEL_ID = "screen_recording_channel"
    private const val CHANNEL_NAME = "Screen Recording"
    private const val CHANNEL_DESCRIPTION = "Used for screen recording service"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // Use LOW importance for less intrusive notification
            ).apply {
                description = CHANNEL_DESCRIPTION
                setSound(null, null) // No sound for recording notification
                enableLights(false)
                enableVibration(false)
            }
            
            val notificationManager = context.getSystemService<NotificationManager>()
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun createNotification(context: Context): Notification {
        // Create an intent that opens the app when the notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Create stop recording action
        val stopIntent = Intent(context, ScreenRecorderService::class.java).apply {
            action = ScreenRecorderService.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Screen Recording")
            .setContentText("Recording in progress...")
            .setSmallIcon(R.drawable.record_circle_line)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Cannot be dismissed by the user
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.record_circle_line, "Stop Recording", stopPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}