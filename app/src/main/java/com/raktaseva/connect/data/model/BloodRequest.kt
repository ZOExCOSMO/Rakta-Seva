package com.raktaseva.connect.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class BloodRequest(
    @DocumentId val id: String = "",
    val hospitalName: String = "",
    val hospitalLocation: GeoPoint? = null,
    val hospitalAddress: String = "",
    val bloodGroup: String = "",
    val unitsNeeded: Int = 1,
    val patientCondition: String = "",
    val urgencyLevel: String = "CRITICAL",
    val requestedBy: String = "",
    val isVerifiedHospital: Boolean = false,
    val aiUrgencyScore: Int = 0,
    val status: String = "ACTIVE",
    val acceptedDonors: List<String> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis()
)