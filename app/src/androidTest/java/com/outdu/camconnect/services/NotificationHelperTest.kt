package com.outdu.camconnect.services

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for NotificationHelper.
 * Covers notification channel creation and notification building with real Android context.
 */
@RunWith(AndroidJUnit4::class)
class NotificationHelperTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun createNotificationChannel_doesNotThrow() {
        NotificationHelper.createNotificationChannel(context)
    }

    @Test
    fun createNotification_returnsNonNullNotification() {
        NotificationHelper.createNotificationChannel(context)
        val notification = NotificationHelper.createNotification(context)
        assertNotNull(notification)
    }

    @Test
    fun createNotification_hasStopAction() {
        NotificationHelper.createNotificationChannel(context)
        val notification = NotificationHelper.createNotification(context)
        assertNotNull(notification.actions)
        assertTrue(notification.actions.isNotEmpty())
    }

    @Test
    fun createNotification_hasContent() {
        NotificationHelper.createNotificationChannel(context)
        val notification = NotificationHelper.createNotification(context)
        assertNotNull(notification)
        assertTrue(notification.flags and android.app.Notification.FLAG_ONGOING_EVENT != 0)
    }

    @Test
    fun notificationChannel_existsAfterCreate() {
        NotificationHelper.createNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel("screen_recording_channel")
            assertNotNull(channel)
        }
    }
}
