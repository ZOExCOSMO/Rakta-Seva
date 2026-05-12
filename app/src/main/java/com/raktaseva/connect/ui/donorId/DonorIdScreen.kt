package com.raktaseva.connect.ui.donorId

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raktaseva.connect.data.model.Donor
import com.raktaseva.connect.ui.theme.*
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorIdScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var donor by remember { mutableStateOf<Donor?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        try {
            val doc = db.collection("donors").document(uid).get().await()
            donor = doc.toObject(Donor::class.java)
        } catch (e: Exception) { }
        isLoading = false
    }

    fun shareIdCard() {
        val d = donor ?: return
        val shareText = """
🩸 Rakta-Seva Connect — Donor ID Card

Name: ${d.name}
Blood Group: ${d.bloodGroup}
Donor ID: ${d.donorId}
City: ${d.city}
Date of Birth: ${d.dateOfBirth}

✓ Verified Donor on Rakta-Seva Connect
        """.trimIndent()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "My Rakta-Seva Donor ID Card")
        }
        context.startActivity(Intent.createChooser(intent, "Share Donor ID"))
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
                    Column {
                        Text("Donor ID Card", fontWeight = FontWeight.Bold,
                            color = TextPrimary, fontSize = 15.sp)
                        Text("Official verification document",
                            color = TextSecondary, fontSize = 11.sp)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(GreenSurface, RoundedCornerShape(8.dp))
                            .border(0.5.dp, GreenPrimary.copy(0.25f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text("✓ ACTIVE", fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = GreenLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RedPrimary)
            }
        } else if (donor == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No donor profile found.", color = TextSecondary)
            }
        } else {
            val d = donor!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── THE ID CARD ──
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(0.5.dp, Color(0xFF3A1515), RoundedCornerShape(18.dp))
                ) {
                    Column {
                        // Card top gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF1A0808), Color(0xFF2E1010), Color(0xFF1A0808))
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            // Watermark
                            Text(
                                d.bloodGroup,
                                fontSize = 80.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White.copy(0.04f),
                                modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 8.dp)
                            )
                            Column {
                                // Header row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Box(
                                            Modifier.size(9.dp)
                                                .background(RedPrimary, RoundedCornerShape(5.dp))
                                        )
                                        Text(
                                            "RAKTA-SEVA CONNECT",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White.copy(0.6f),
                                            letterSpacing = 0.08.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .border(0.5.dp, Color.White.copy(0.15f), RoundedCornerShape(5.dp))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text("GOV. OF KARNATAKA", fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold, color = Color.White.copy(0.4f))
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    // Photo / initials box
                                    Box(
                                        modifier = Modifier
                                            .size(width = 72.dp, height = 86.dp)
                                            .background(Color(0xFF2A1515), RoundedCornerShape(10.dp))
                                            .border(1.5.dp, RedPrimary.copy(0.35f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            d.name.take(2).uppercase(),
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White.copy(0.8f)
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        IdField("Full Name", d.name)
                                        IdField("Blood Group", d.bloodGroup,
                                            valueSize = 22.sp, valueColor = RedLight)
                                        IdField("Donor ID", d.donorId, mono = true)
                                    }
                                }
                            }
                        }

                        // Card bottom bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0A0A0C))
                                .padding(12.dp, 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Barcode
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    listOf(3,1,2,1,4,1,2,3,1,2,1,3,1,4,2,1,3,1,2,1,3,2,1,4,1,2,1,3,1,2)
                                        .forEach { h ->
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .fillMaxHeight((8 + h * 4) / 44f)
                                                    .align(Alignment.Bottom)
                                                    .background(Color.White.copy(0.7f))
                                            )
                                        }
                                }
                                Text(
                                    d.donorId.takeLast(8).replace("-", "  "),
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White.copy(0.3f),
                                    letterSpacing = 0.1.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Status chip
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier
                                        .background(GreenPrimary.copy(0.1f), RoundedCornerShape(20.dp))
                                        .border(0.5.dp, GreenPrimary.copy(0.25f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Box(Modifier.size(5.dp).background(GreenLight, RoundedCornerShape(3.dp)))
                                    Text("Eligible", fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold, color = GreenLight)
                                }
                                // QR placeholder
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color.White.copy(0.05f), RoundedCornerShape(7.dp))
                                        .border(0.5.dp, Color.White.copy(0.1f), RoundedCornerShape(7.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("QR", fontSize = 12.sp,
                                        color = Color.White.copy(0.4f), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ── DONOR DETAILS ──
                SectionLabel("Donor Details")
                DarkCard {
                    Column {
                        DetailRow("👤", "Full Name", d.name, false)
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        DetailRow("📅", "Date of Birth", d.dateOfBirth.ifEmpty { "—" }, false)
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        DetailRow("📍", "City / Taluka",
                            if (d.taluka.isNotEmpty()) "${d.city}, ${d.taluka}" else d.city, false)
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        DetailRow("🩸", "Blood Group", "${d.bloodGroup} Positive",
                            false, valueColor = RedLight)
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        DetailRow("📞", "Contact", d.phone.ifEmpty { "—" }, true)
                    }
                }

                // ── VERIFICATION STATUS ──
                SectionLabel("Verification Status")
                DarkCard {
                    Column {
                        VerificationRow("📱", "Mobile OTP",
                            "${d.phone} · Verified", true, false)
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        VerificationRow("🩸", "Blood Group",
                            "Registered as ${d.bloodGroup}", true, false)
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        VerificationRow("🪪", "Aadhaar Card",
                            if (d.aadhaarVerified) "KYC Verified" else "Pending verification",
                            d.aadhaarVerified, false)
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        VerificationRow("🏥", "Medical Fitness",
                            if (d.hemoglobin > 0) "Hb: ${d.hemoglobin} g/dL" else "Not submitted",
                            d.hemoglobin > 0, true)
                    }
                }

                // ── VALIDITY ──
                SectionLabel("Validity")
                DarkCard {
                    Column {
                        DetailRow("🪪", "Donor ID", d.donorId, false)
                        HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                        DetailRow("📅", "Member Since",
                            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                .format(java.util.Date(d.createdAtMillis)), true)
                    }
                }

                // ── ACTION BUTTONS ──
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { shareIdCard() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BgElevated)
                ) {
                    Text("🔗  Share ID Card", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { shareIdCard() }, // reuses share for now
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedDark)
                ) {
                    Text("⬇  Download PDF Copy", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun IdField(
    label: String,
    value: String,
    valueSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    valueColor: Color = Color.White,
    mono: Boolean = false
) {
    Column {
        Text(label.uppercase(), fontSize = 9.sp, color = Color.White.copy(0.35f),
            fontWeight = FontWeight.Bold, letterSpacing = 0.08.sp)
        Text(value, fontSize = valueSize, fontWeight = FontWeight.Bold, color = valueColor,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            letterSpacing = if (mono) 0.05.sp else 0.sp)
    }
}

@Composable
fun DarkCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.padding(horizontal = 12.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(0.5.dp, BorderSubtle)
    ) { content() }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        fontSize = 11.sp, fontWeight = FontWeight.Bold,
        color = TextTertiary, letterSpacing = 0.07.sp
    )
}

@Composable
fun DetailRow(icon: String, label: String, value: String,
              isLast: Boolean, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier.padding(11.dp, 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(icon, fontSize = 16.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(label.uppercase(), fontSize = 9.5.sp, color = TextTertiary,
                fontWeight = FontWeight.Bold, letterSpacing = 0.05.sp)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                color = valueColor, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun VerificationRow(icon: String, title: String, sub: String,
                    verified: Boolean, isLast: Boolean) {
    Row(
        modifier = Modifier.padding(11.dp, 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(RedPrimary.copy(0.1f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) { Text(icon, fontSize = 18.sp) }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(sub, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 1.dp))
        }
        if (verified) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(GreenSurface, RoundedCornerShape(11.dp))
                    .border(0.5.dp, GreenPrimary.copy(0.3f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 12.sp, color = GreenLight, fontWeight = FontWeight.Bold)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(GoldSurface, RoundedCornerShape(11.dp))
                    .border(0.5.dp, Gold.copy(0.3f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) { Text("⏳", fontSize = 11.sp) }
        }
    }
}