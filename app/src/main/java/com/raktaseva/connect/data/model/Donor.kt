package com.raktaseva.connect.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class Donor(
    @DocumentId val id: String = "",
    val name: String = "",
    val phone: String = "",
    val bloodGroup: String = "",
    val dateOfBirth: String = "",
    val location: GeoPoint? = null,
    val city: String = "Bengaluru",
    val taluka: String = "",
    val lastDonationDateMillis: Long = 0L,
    val isAvailable: Boolean = true,
    val aadhaarVerified: Boolean = false,
    val fcmToken: String = "",
    val totalDonations: Int = 0,
    val trustScore: Int = 0,
    val donorId: String = "",
    val hemoglobin: Double = 0.0,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun isEligibleNow(): Boolean {
        val ninetyDaysMs = 90L * 24 * 60 * 60 * 1000
        return isAvailable &&
                (System.currentTimeMillis() - lastDonationDateMillis) >= ninetyDaysMs
    }

    fun daysUntilEligible(): Int {
        val ninetyDaysMs = 90L * 24 * 60 * 60 * 1000
        val elapsed = System.currentTimeMillis() - lastDonationDateMillis
        return if (elapsed >= ninetyDaysMs) 0
        else ((ninetyDaysMs - elapsed) / (24 * 60 * 60 * 1000)).toInt()
    }
}