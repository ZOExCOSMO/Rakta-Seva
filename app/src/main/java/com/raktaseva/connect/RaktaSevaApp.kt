package com.raktaseva.connect

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.raktaseva.connect.utils.Constants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RaktaSevaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID,
                "Emergency Blood Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent blood donation requests near you"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                enableLights(true)
                lightColor = android.graphics.Color.RED
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}