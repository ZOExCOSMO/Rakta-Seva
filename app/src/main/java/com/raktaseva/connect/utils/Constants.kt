package com.raktaseva.connect.utils

object Constants {
    // Replace with your actual deployed backend URL
    const val BASE_URL = "https://rakta-seva-backend.onrender.com/"

    const val CHANNEL_ID = "emergency_alerts"
    const val RADIUS_KM  = 10.0

    const val PREF_DONOR_ID    = "donor_id"
    const val PREF_BLOOD_GROUP = "blood_group"
    const val PREF_FCM_TOKEN   = "fcm_token"

    val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val URGENCY_LEVELS = listOf(
        "Critical — immediate",
        "Urgent — within 3 hours",
        "Scheduled surgery"
    )
}