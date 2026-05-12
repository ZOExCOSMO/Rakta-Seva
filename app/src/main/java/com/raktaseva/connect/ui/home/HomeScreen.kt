package com.raktaseva.connect.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.raktaseva.connect.data.model.BloodRequest
import com.raktaseva.connect.data.model.Donor
import com.raktaseva.connect.ui.theme.*
import com.raktaseva.connect.utils.Constants
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAlertClick: (String) -> Unit,
    onChatClick: () -> Unit,
    onDonorIdClick: () -> Unit,
    onProfileClick: () -> Unit,
    onCreateRequestClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val donor by viewModel.donor.collectAsState()
    val requests by viewModel.requests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val view = LocalView.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val filteredRequests = if (selectedFilter == "All") requests
    else requests.filter { it.bloodGroup == selectedFilter }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "RAKTA-SEVA CONNECT",
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            letterSpacing = 0.08.sp
                        )
                        Text(
                            "Bengaluru · ${filteredRequests.size} alerts nearby",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (filteredRequests.isNotEmpty()) onAlertClick(filteredRequests[0].id)
                    }) {
                        Icon(Icons.Default.Notifications, null, tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard)
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedTab,
                onHomeClick = { selectedTab = 0 },
                onChatClick = { selectedTab = 1; onChatClick() },
                onIdClick = { selectedTab = 2; onDonorIdClick() },
                onProfileClick = { selectedTab = 3; onProfileClick() }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = RedPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item { donor?.let { DonorDashboardCard(it, onDonorIdClick) } }

                item {
                    SosButton(onClick = {
                        ViewCompat.performHapticFeedback(
                            view, HapticFeedbackConstantsCompat.REJECT
                        )
                        if (filteredRequests.isNotEmpty()) onAlertClick(filteredRequests[0].id)
                    })
                }

                item {
                    val filters = listOf("All") + Constants.BLOOD_GROUPS
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { viewModel.setFilter(filter) },
                                label = {
                                    Text(filter, fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = RedDark,
                                    selectedLabelColor = Color.White,
                                    containerColor = BgCard,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedFilter == filter,
                                    borderColor = BorderSubtle,
                                    selectedBorderColor = RedPrimary
                                )
                            )
                        }
                    }
                }

                item { SectionTitle("Emergency Requests") }

                if (filteredRequests.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No active requests for $selectedFilter",
                                color = TextSecondary, fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    itemsIndexed(filteredRequests) { index, req ->
                        StaggeredAlertCard(
                            request = req,
                            index = index,
                            onClick = { onAlertClick(req.id) }
                        )
                    }
                }

                item { SectionTitle("Quick Access") }
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            QuickAccessCard("🤖", "Ask AI",
                                Modifier.weight(1f), onClick = onChatClick)
                            QuickAccessCard("🪪", "Donor ID",
                                Modifier.weight(1f), onClick = onDonorIdClick)
                        }
                        Card(
                            onClick = onCreateRequestClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = RedDeep),
                            border = BorderStroke(0.5.dp, RedBorder.copy(0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("🏥", fontSize = 24.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Post Emergency Request",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White, fontSize = 14.sp
                                    )
                                    Text(
                                        "Notify nearby donors instantly",
                                        color = Color.White.copy(0.6f), fontSize = 11.sp
                                    )
                                }
                                Text("›", fontSize = 20.sp, color = Color.White.copy(0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    selectedTab: Int,
    onHomeClick: () -> Unit,
    onChatClick: () -> Unit,
    onIdClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = BgCard,
        tonalElevation = 0.dp,
        modifier = Modifier.border(BorderStroke(0.5.dp, BorderSubtle))
    ) {
        listOf(
            Triple(Icons.Default.Home, "HOME", 0),
            Triple(Icons.Default.Chat, "CHAT", 1),
            Triple(Icons.Default.Badge, "ID CARD", 2),
            Triple(Icons.Default.Person, "PROFILE", 3)
        ).forEachIndexed { index, (icon, label, tab) ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = {
                    when (index) {
                        0 -> onHomeClick()
                        1 -> onChatClick()
                        2 -> onIdClick()
                        3 -> onProfileClick()
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = {
                    Text(label, fontSize = 9.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.05.sp)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RedLight,
                    selectedTextColor = RedLight,
                    unselectedIconColor = TextTertiary,
                    unselectedTextColor = TextTertiary,
                    indicatorColor = RedSurface
                )
            )
        }
    }
}

@Composable
fun StaggeredAlertCard(request: BloodRequest, index: Int, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 50L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = 0.7f,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetY = { it / 2 }
        )
    ) {
        AlertCard(request = request, onClick = onClick)
    }
}

@Composable
fun AlertCard(request: BloodRequest, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "cardScale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .scale(scale),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(0.5.dp, BorderSubtle),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "dot")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 0.15f,
                    animationSpec = infiniteRepeatable(
                        tween(900, easing = EaseInOut), RepeatMode.Reverse
                    ), label = "dotAlpha"
                )
                Box(
                    Modifier.size(8.dp)
                        .background(RedPrimary.copy(alpha), RoundedCornerShape(4.dp))
                )
                Box(Modifier.width(1.dp).height(32.dp).background(BorderSubtle))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(request.hospitalName, fontWeight = FontWeight.SemiBold,
                        color = TextPrimary, fontSize = 13.sp)
                    if (request.isVerifiedHospital) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(BluePrimary.copy(0.15f), RoundedCornerShape(5.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("✓ VERIFIED", fontSize = 9.sp,
                                fontWeight = FontWeight.Bold, color = BlueLight)
                        }
                    }
                }
                Text(
                    "${request.bloodGroup} · ${request.patientCondition} · ${request.unitsNeeded} unit(s)",
                    color = TextSecondary, fontSize = 11.5.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Row(
                    modifier = Modifier.padding(top = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusPill(request.urgencyLevel, RedSurface, RedLight)
                    if (request.aiUrgencyScore > 0) {
                        StatusPill("AI ${request.aiUrgencyScore}",
                            RedPrimary.copy(0.12f), RedLight)
                    }
                }
            }
            Text("›", fontSize = 20.sp, color = TextTertiary,
                modifier = Modifier.align(Alignment.CenterVertically))
        }
    }
}

@Composable
fun StatusPill(text: String, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 10.sp, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SosButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.25f, stiffness = 200f),
        label = "sosScale"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Restart),
        label = "pulseAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 2.5f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Restart),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth()
            .scale(scale)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RedDeep),
            border = BorderStroke(0.5.dp, RedBorder.copy(0.4f)),
            interactionSource = interactionSource
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .scale(pulseScale)
                            .background(RedPrimary.copy(pulseAlpha), RoundedCornerShape(21.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(RedPrimary.copy(0.2f), RoundedCornerShape(21.dp))
                            .border(0.5.dp, RedPrimary.copy(0.4f), RoundedCornerShape(21.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("🚨", fontSize = 18.sp) }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("SOS EMERGENCY", fontWeight = FontWeight.ExtraBold,
                        color = Color.White, fontSize = 15.sp, letterSpacing = 0.05.sp)
                    Text("One tap · auto-fills your location",
                        color = Color.White.copy(0.55f), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun DonorDashboardCard(donor: Donor, onDonorIdClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1A0808), Color(0xFF2E1010))))
            .clickable { onDonorIdClick() }
            .padding(16.dp)
    ) {
        Text(
            donor.bloodGroup,
            fontSize = 96.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White.copy(0.04f),
            modifier = Modifier.align(Alignment.BottomEnd)
        )
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "logoDot")
                    val dotScale by infiniteTransition.animateFloat(
                        initialValue = 1f, targetValue = 1.4f,
                        animationSpec = infiniteRepeatable(
                            tween(800, easing = EaseInOut), RepeatMode.Reverse
                        ), label = "dotScale"
                    )
                    Box(
                        Modifier.size(8.dp).scale(dotScale)
                            .background(RedPrimary, RoundedCornerShape(4.dp))
                    )
                    Text(
                        "RAKTA-SEVA CONNECT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(0.6f),
                        letterSpacing = 0.08.sp
                    )
                }

                // Badge — fixed no overflow
                val isEligible = donor.isAvailable && donor.isEligibleNow()
                val badgeText = when {
                    !donor.isAvailable -> "COOLDOWN"
                    !donor.isEligibleNow() -> "${donor.daysUntilEligible()}d LEFT"
                    else -> "ELIGIBLE"
                }
                Box(
                    modifier = Modifier
                        .background(
                            if (isEligible) GreenSurface else RedSurface,
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            0.5.dp,
                            if (isEligible) GreenPrimary.copy(0.3f)
                            else RedPrimary.copy(0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEligible) GreenLight else RedLight,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 66.dp, height = 78.dp)
                        .background(Color(0xFF2A1515), RoundedCornerShape(12.dp))
                        .border(1.5.dp, RedPrimary.copy(0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        donor.name.take(2).uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(0.8f)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column {
                        Text("FULL NAME", fontSize = 9.sp,
                            color = Color.White.copy(0.35f),
                            fontWeight = FontWeight.Bold, letterSpacing = 0.08.sp)
                        Text(donor.name, fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text("BLOOD GROUP", fontSize = 9.sp,
                            color = Color.White.copy(0.35f),
                            fontWeight = FontWeight.Bold, letterSpacing = 0.08.sp)
                        Text(donor.bloodGroup, fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold, color = RedLight)
                    }
                    Column {
                        Text("DONOR ID", fontSize = 9.sp,
                            color = Color.White.copy(0.35f),
                            fontWeight = FontWeight.Bold, letterSpacing = 0.08.sp)
                        Text(donor.donorId, fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(0.6f),
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.06.sp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf(
                    donor.totalDonations.toString() to "DONATIONS",
                    "${donor.trustScore}%" to "TRUST",
                    donor.city to "CITY"
                ).forEach { (v, l) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(0.04f), RoundedCornerShape(10.dp))
                            .border(0.5.dp, Color.White.copy(0.07f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(v, fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White, maxLines = 1)
                            Text(l, fontSize = 8.sp,
                                color = Color.White.copy(0.35f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.05.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAccessCard(
    icon: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "quickScale"
    )
    Card(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = BorderStroke(0.5.dp, BorderSubtle),
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 28.sp)
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title.uppercase(),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        fontSize = 10.sp, fontWeight = FontWeight.Bold,
        color = TextTertiary, letterSpacing = 0.07.sp
    )
}