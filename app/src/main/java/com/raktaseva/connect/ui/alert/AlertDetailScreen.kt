package com.raktaseva.connect.ui.alert

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.raktaseva.connect.data.model.BloodRequest
import com.raktaseva.connect.ui.theme.*
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDetailScreen(requestId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var request by remember { mutableStateOf<BloodRequest?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }
    var isDonating by remember { mutableStateOf(false) }
    var isDonorEligible by remember { mutableStateOf(true) }
    var donorStatusMsg by remember { mutableStateOf("") }

    LaunchedEffect(requestId) {
        try {
            val doc = db.collection("blood_requests")
                .document(requestId).get().await()
            request = doc.toObject(BloodRequest::class.java)
        } catch (e: Exception) { }

        // Check donor eligibility
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                val donorDoc = db.collection("donors")
                    .document(uid).get().await()
                val isAvailable = donorDoc.getBoolean("isAvailable") ?: true
                val lastDonation = donorDoc.getLong("lastDonationDateMillis") ?: 0L
                val ninetyDaysMs = 90L * 24 * 60 * 60 * 1000
                val elapsed = System.currentTimeMillis() - lastDonation
                val daysLeft = ((ninetyDaysMs - elapsed) / (24 * 60 * 60 * 1000)).toInt()

                if (!isAvailable || elapsed < ninetyDaysMs) {
                    isDonorEligible = false
                    donorStatusMsg = if (daysLeft > 0)
                        "You are in cooldown. Eligible in $daysLeft days."
                    else
                        "You are marked unavailable. Enable in Profile settings."
                }
            } catch (e: Exception) { }
        }
        isLoading = false
    }

    fun openGoogleMaps() {
        val req = request ?: return
        val lat = req.hospitalLocation?.latitude
        val lng = req.hospitalLocation?.longitude
        val address = req.hospitalAddress.ifEmpty { req.hospitalName }

        val intent = if (lat != null && lng != null &&
            lat != 12.9716 && lng != 77.5946) {
            val uri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
            Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
        } else {
            val encodedAddress = Uri.encode(address)
            val uri = Uri.parse("google.navigation:q=$encodedAddress&mode=d")
            Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val encodedAddress = Uri.encode(address)
            context.startActivity(
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$encodedAddress"))
            )
        }
    }

    fun acceptDonation() {
        if (!isDonorEligible) return
        val uid = auth.currentUser?.uid ?: return
        isDonating = true

        db.collection("blood_requests").document(requestId)
            .update("acceptedDonors", FieldValue.arrayUnion(uid))

        db.collection("donors").document(uid)
            .update(
                "totalDonations", FieldValue.increment(1),
                "lastDonationDateMillis", System.currentTimeMillis(),
                "isAvailable", false
            )
            .addOnSuccessListener {
                isDonating = false
                showAcceptDialog = true
            }
            .addOnFailureListener {
                isDonating = false
                showAcceptDialog = true
            }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextSecondary)
                    }
                },
                title = {
                    Text("Emergency Alert", fontWeight = FontWeight.Bold,
                        color = TextPrimary, fontSize = 14.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(BgCard)
                    .border(BorderStroke(0.5.dp, BorderSubtle))
            ) {
                // Cooldown warning banner
                if (!isDonorEligible) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RedSurface)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "⏱ $donorStatusMsg",
                            color = RedLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary),
                        border = BorderStroke(0.5.dp, BorderMid)
                    ) {
                        Text("Decline", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { acceptDonation() },
                        enabled = !isDonating && isDonorEligible,
                        modifier = Modifier.weight(2f).height(50.dp),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDonorEligible) RedDark else BorderMid,
                            disabledContainerColor = BorderMid
                        )
                    ) {
                        if (isDonating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (isDonorEligible) "✓ Accept & Donate"
                                else "⏱ In Cooldown",
                                fontWeight = FontWeight.Bold,
                                color = if (isDonorEligible) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RedPrimary)
            }
        } else if (request == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Request not found", color = TextSecondary)
            }
        } else {
            val req = request!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1A0808), Color(0xFF2A1010))
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(
                                    RedPrimary.copy(0.1f),
                                    RoundedCornerShape(44.dp)
                                )
                                .border(
                                    2.dp,
                                    RedLight.copy(0.5f),
                                    RoundedCornerShape(44.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(req.bloodGroup, fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White)
                                Text("URGENT", fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(0.5f),
                                    letterSpacing = 0.1.sp)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Blood Required Now", fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text(
                            req.hospitalAddress.ifEmpty { req.hospitalName },
                            fontSize = 12.sp,
                            color = Color.White.copy(0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .background(
                                    RedPrimary.copy(0.12f),
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    0.5.dp,
                                    RedPrimary.copy(0.25f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                "🤖 AI Score: ${req.aiUrgencyScore} · High Priority",
                                fontSize = 11.sp, color = RedLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Details card
                Card(
                    modifier = Modifier.padding(12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(0.5.dp, BorderSubtle)
                ) {
                    listOf(
                        "Hospital" to "${req.hospitalName}${
                            if (req.isVerifiedHospital) " ✓" else ""}",
                        "Address" to req.hospitalAddress.ifEmpty { "—" },
                        "Blood group" to req.bloodGroup,
                        "Units needed" to "${req.unitsNeeded} units",
                        "Patient condition" to req.patientCondition,
                        "Urgency" to req.urgencyLevel,
                        "Coordinator" to req.requestedBy.ifEmpty {
                            "Will contact you after accept" }
                    ).forEachIndexed { i, (label, value) ->
                        Row(
                            modifier = Modifier
                                .padding(15.dp, 12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                label,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(0.4f)
                            )
                            Text(
                                value,
                                color = if (label == "Blood group") RedLight
                                else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.6f)
                            )
                        }
                        if (i < 6) HorizontalDivider(
                            color = BorderSubtle, thickness = 0.5.dp)
                    }
                }

                // Privacy note
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(BgElevated, RoundedCornerShape(12.dp))
                        .border(0.5.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(13.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Text("🔐", fontSize = 14.sp)
                    Text(
                        "Your phone number is shared only after you accept. " +
                                "Tap Accept to see live navigation to the hospital.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    // Accept success dialog
    if (showAcceptDialog) {
        Dialog(onDismissRequest = { showAcceptDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(0.5.dp, BorderMid)
            ) {
                Column(
                    modifier = Modifier.padding(26.dp, 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🩸", fontSize = 36.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("You're a Hero!", fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold, color = TextPrimary)

                    val coordinatorInfo = request?.requestedBy ?: ""
                    if (coordinatorInfo.isNotEmpty()) {
                        Text(
                            "Coordinator will contact you:\n$coordinatorInfo",
                            color = TextSecondary, fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp),
                            lineHeight = 20.sp
                        )
                    } else {
                        Text(
                            "Thank you! Coordinator will contact you soon.",
                            color = TextSecondary, fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp),
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RedSurface, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            "⏱ You are now in 90-day cooldown",
                            color = RedLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            showAcceptDialog = false
                            showMapDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A8A))
                    ) {
                        Text("📍 Navigate to Hospital",
                            color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = {
                        showAcceptDialog = false
                        onBack()
                    }) {
                        Text("Later", color = TextSecondary)
                    }
                }
            }
        }
    }

    // Map dialog
    if (showMapDialog) {
        val req = request
        Dialog(onDismissRequest = { showMapDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(0.5.dp, BorderMid)
            ) {
                Column(
                    modifier = Modifier.padding(26.dp, 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🗺️", fontSize = 36.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Opening Google Maps", fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text(
                        "${req?.hospitalName ?: "Hospital"}\n" +
                                "${req?.hospitalAddress ?: ""}",
                        color = TextSecondary, fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp),
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            openGoogleMaps()
                            showMapDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedDark)
                    ) {
                        Text("Open Navigation",
                            color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}