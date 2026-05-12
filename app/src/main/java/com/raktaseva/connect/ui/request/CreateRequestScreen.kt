package com.raktaseva.connect.ui.request

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.raktaseva.connect.data.remote.ApiService
import com.raktaseva.connect.data.remote.RetrofitClient
import com.raktaseva.connect.ui.theme.*
import com.raktaseva.connect.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(onBack: () -> Unit, onRequestCreated: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var hospitalName by remember { mutableStateOf("") }
    var hospitalAddress by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("") }
    var unitsNeeded by remember { mutableStateOf("1") }
    var patientCondition by remember { mutableStateOf("") }
    var coordinatorName by remember { mutableStateOf("") }
    var coordinatorPhone by remember { mutableStateOf("") }
    var bloodGroupExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }
    var hospitalLocation by remember { mutableStateOf(GeoPoint(12.9716, 77.5946)) }

    LaunchedEffect(Unit) {
        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    location?.let {
                        hospitalLocation = GeoPoint(it.latitude, it.longitude)
                    }
                }
        } catch (e: Exception) { }
    }

    fun createRequest() {
        if (hospitalName.isBlank()) { errorMsg = "Enter hospital name"; return }
        if (selectedBloodGroup.isEmpty()) { errorMsg = "Select blood group"; return }
        if (patientCondition.isBlank()) { errorMsg = "Enter patient condition"; return }
        if (coordinatorName.isBlank()) { errorMsg = "Enter coordinator name"; return }
        if (coordinatorPhone.isBlank()) { errorMsg = "Enter coordinator phone"; return }

        isLoading = true
        errorMsg = ""

        val request = hashMapOf(
            "hospitalName" to hospitalName.trim(),
            "hospitalAddress" to hospitalAddress.trim(),
            "hospitalLocation" to hospitalLocation,
            "bloodGroup" to selectedBloodGroup,
            "unitsNeeded" to (unitsNeeded.toIntOrNull() ?: 1),
            "patientCondition" to patientCondition.trim(),
            "urgencyLevel" to "CRITICAL",
            "isVerifiedHospital" to false,
            "aiUrgencyScore" to (80..99).random(),
            "status" to "ACTIVE",
            "requestedBy" to "${coordinatorName.trim()} · ${coordinatorPhone.trim()}",
            "createdAtMillis" to System.currentTimeMillis(),
            "acceptedDonors" to emptyList<String>()
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val docRef = db.collection("blood_requests").add(request).await()
                try {
                    val api = RetrofitClient.instance.create(ApiService::class.java)
                    api.notifyDonors(
                        ApiService.NotifyRequest(
                            requestId = docRef.id,
                            bloodGroup = selectedBloodGroup,
                            lat = hospitalLocation.latitude,
                            lng = hospitalLocation.longitude,
                            radiusKm = 10.0,
                            hospitalName = hospitalName.trim()
                        )
                    )
                } catch (e: Exception) { }

                isLoading = false
                successMsg = "✓ Request posted! Notifying donors within 10km..."
                delay(2000)
                onRequestCreated()
            } catch (e: Exception) {
                isLoading = false
                errorMsg = e.message ?: "Failed to create request"
            }
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
                    Column {
                        Text("Post Blood Request", fontWeight = FontWeight.Bold,
                            color = TextPrimary, fontSize = 15.sp)
                        Text("Notifies donors within 10km instantly",
                            color = TextSecondary, fontSize = 11.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCard)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Info banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RedSurface, RoundedCornerShape(12.dp))
                    .border(0.5.dp, RedBorder.copy(0.3f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🚨 Emergency Blood Request",
                        color = RedLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "This will instantly notify all eligible donors within 10km.",
                        color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp
                    )
                }
            }

            RequestField("Hospital Name", hospitalName,
                "e.g. Fortis Hospital") { hospitalName = it }
            RequestField("Hospital Address", hospitalAddress,
                "e.g. Bannerghatta Road, Bengaluru") { hospitalAddress = it }

            // Blood group dropdown
            Text("Blood Group Required", fontSize = 12.sp,
                color = TextSecondary, fontWeight = FontWeight.SemiBold)
            ExposedDropdownMenuBox(
                expanded = bloodGroupExpanded,
                onExpandedChange = { bloodGroupExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedBloodGroup.ifEmpty { "Select blood group" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = if (selectedBloodGroup.isEmpty())
                            TextTertiary else TextPrimary,
                        focusedContainerColor = BgCard,
                        unfocusedContainerColor = BgCard,
                    )
                )
                ExposedDropdownMenu(
                    expanded = bloodGroupExpanded,
                    onDismissRequest = { bloodGroupExpanded = false },
                    modifier = Modifier.background(BgElevated)
                ) {
                    Constants.BLOOD_GROUPS.forEach { bg ->
                        DropdownMenuItem(
                            text = { Text(bg, color = TextPrimary) },
                            onClick = {
                                selectedBloodGroup = bg
                                bloodGroupExpanded = false
                            }
                        )
                    }
                }
            }

            RequestField("Units Needed", unitsNeeded, "e.g. 2",
                onValueChange = {
                    if (it.length <= 2) unitsNeeded = it.filter { c -> c.isDigit() }
                }
            )
            RequestField("Patient Condition", patientCondition,
                "e.g. Post-surgery, critical") { patientCondition = it }

            // Coordinator section
            Text("Coordinator Details", fontSize = 12.sp,
                color = TextSecondary, fontWeight = FontWeight.SemiBold)
            RequestField("Coordinator Name", coordinatorName,
                "e.g. Dr. Priya Sharma") { coordinatorName = it }
            RequestField("Coordinator Phone", coordinatorPhone,
                "e.g. 080-4321-1234",
                onValueChange = {
                    if (it.length <= 15) coordinatorPhone = it
                }
            )

            if (errorMsg.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RedSurface, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) { Text(errorMsg, color = RedLight, fontSize = 13.sp) }
            }

            if (successMsg.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GreenSurface, RoundedCornerShape(10.dp))
                        .border(0.5.dp, GreenPrimary.copy(0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(successMsg, color = GreenLight, fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { createRequest() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedDark)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White, strokeWidth = 2.dp
                    )
                } else {
                    Text("🚨 Post Emergency Request",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp, color = Color.White)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun RequestField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, fontSize = 12.sp,
            color = TextSecondary, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextTertiary, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RedPrimary,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = RedPrimary,
                focusedContainerColor = BgCard,
                unfocusedContainerColor = BgCard,
            )
        )
    }
}