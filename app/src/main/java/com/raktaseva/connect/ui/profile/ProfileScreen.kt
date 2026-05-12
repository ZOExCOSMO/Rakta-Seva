package com.raktaseva.connect.ui.profile

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raktaseva.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val donor by viewModel.donor.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var notifToggle by remember { mutableStateOf(true) }
    var anonToggle by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = BgCard,
            title = {
                Text("Sign Out", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to sign out?", color = TextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.signOut(onSignOut) },
                    colors = ButtonDefaults.buttonColors(containerColor = RedDark)
                ) { Text("Sign Out", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
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
                    Text("My Profile", fontWeight = FontWeight.Bold,
                        color = TextPrimary, fontSize = 15.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RedPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile hero
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
                                .size(64.dp)
                                .background(RedPrimary.copy(0.12f), RoundedCornerShape(32.dp))
                                .border(1.5.dp, RedPrimary.copy(0.3f), RoundedCornerShape(32.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                donor?.name?.take(2)?.uppercase() ?: "?",
                                fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(donor?.name ?: "Unknown",
                            fontSize = 17.sp, fontWeight = FontWeight.ExtraBold,
                            color = Color.White)
                        Text(donor?.phone ?: "",
                            color = Color.White.copy(0.5f), fontSize = 12.sp,
                            modifier = Modifier.padding(top = 3.dp))
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .background(GreenPrimary.copy(0.1f), RoundedCornerShape(20.dp))
                                .border(0.5.dp, GreenPrimary.copy(0.2f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("✓", fontSize = 11.sp,
                                color = GreenLight, fontWeight = FontWeight.Bold)
                            Text("Registered Donor",
                                fontSize = 11.sp, color = GreenLight,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Stats grid
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        (donor?.bloodGroup ?: "?") to "Blood Group",
                        "${donor?.trustScore ?: 0}%" to "Trust Score",
                        "${donor?.totalDonations ?: 0}" to "Donations"
                    ).forEach { (v, l) ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(13.dp),
                            colors = CardDefaults.cardColors(containerColor = BgCard),
                            border = BorderStroke(0.5.dp, BorderSubtle)
                        ) {
                            Column(Modifier.padding(11.dp)) {
                                Text(v, fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (l == "Blood Group") RedLight else TextPrimary)
                                Text(l, fontSize = 10.sp, color = TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }

                // Settings
                Spacer(Modifier.height(2.dp))
                Text("Settings",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                Card(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(0.5.dp, BorderSubtle)
                ) {
                    // Available to donate — saves to Firestore
                    ToggleRow(
                        title = "Available to donate",
                        sub = "Auto-off 90 days after donation",
                        checked = donor?.isAvailable ?: true,
                        onCheckedChange = { viewModel.updateAvailability(it) }
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                    // Notifications — saves to Firestore
                    ToggleRow(
                        title = "Push notifications",
                        sub = "FCM emergency alerts nearby",
                        checked = notifToggle,
                        onCheckedChange = {
                            notifToggle = it
                            viewModel.updateField("notificationsEnabled", it)
                        }
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)

                    // Anonymous mode — saves to Firestore
                    ToggleRow(
                        title = "Anonymous mode",
                        sub = "Hide identity until you accept",
                        checked = anonToggle,
                        onCheckedChange = {
                            anonToggle = it
                            viewModel.updateField("anonymousMode", it)
                        }
                    )
                }

                // Donor details
                Text("Donor Details",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
                Card(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = BorderStroke(0.5.dp, BorderSubtle)
                ) {
                    listOf(
                        "📅" to ("Date of Birth" to (donor?.dateOfBirth ?: "—")),
                        "📍" to ("City" to (donor?.city ?: "—")),
                        "🪪" to ("Donor ID" to (donor?.donorId ?: "—"))
                    ).forEachIndexed { i, (icon, pair) ->
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(icon, fontSize = 16.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pair.first.uppercase(),
                                    fontSize = 9.5.sp, color = TextTertiary,
                                    fontWeight = FontWeight.Bold)
                                Text(pair.second, fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold, color = TextPrimary,
                                    modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                        if (i < 2) HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                    }
                }

                // Sign out
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showSignOutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, RedPrimary.copy(0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedLight)
                ) { Text("Sign Out", fontWeight = FontWeight.SemiBold) }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ToggleRow(
    title: String,
    sub: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(sub, fontSize = 11.sp, color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GreenPrimary,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = BgElevated
            )
        )
    }
}