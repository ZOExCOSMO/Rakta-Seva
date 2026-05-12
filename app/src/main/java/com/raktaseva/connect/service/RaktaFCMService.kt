package com.raktaseva.connect.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.raktaseva.connect.MainActivity
import com.raktaseva.connect.R
import com.raktaseva.connect.utils.Constants

class RaktaFCMService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        showAlertNotification(
            requestId  = data["requestId"] ?: "",
            bloodGroup = data["bloodGroup"] ?: "Unknown",
            hospital   = data["hospital"]  ?: "Nearby Hospital",
            distKm     = data["distanceKm"] ?: "?"
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("donors").document(uid)
            .update("fcmToken", token)
    }

    private fun showAlertNotification(
        requestId: String, bloodGroup: String,
        hospital: String, distKm: String
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("requestId", requestId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            this, requestId.hashCode(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_blood_drop)
            .setContentTitle("🩸 Urgent: $bloodGroup Blood Needed")
            .setContentText("$hospital • $distKm km away — tap to respond")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("🏥 $hospital needs $bloodGroup blood urgently.\n📍 You are $distKm km away.\nTap to accept and save a life."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setColor(0xC0392B)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(requestId.hashCode(), notification)
    }
}