package com.raktaseva.connect.ui.register

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.messaging.FirebaseMessaging
import com.raktaseva.connect.ui.theme.*
import com.raktaseva.connect.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onRegistrationComplete: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var selectedBloodGroup by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var taluka by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var bloodGroupExpanded by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(1) }

    fun validateStep1(): Boolean {
        return when {
            name.isBlank() -> { errorMsg = "Please enter your full name"; false }
            dob.isBlank() -> { errorMsg = "Please enter your date of birth"; false }
            dob.length != 10 -> { errorMsg = "Enter date as DD/MM/YYYY"; false }
            city.isBlank() -> { errorMsg = "Please enter your city"; false }
            else -> { errorMsg = ""; true }
        }
    }

    fun validateStep2(): Boolean {
        return when {
            selectedBloodGroup.isEmpty() -> { errorMsg = "Please select your blood group"; false }
            weight.isBlank() -> { errorMsg = "Please enter your weight"; false }
            (weight.toIntOrNull() ?: 0) < 50 -> {
                errorMsg = "Weight must be at least 50 kg to donate"; false
            }
            !agreedToTerms -> { errorMsg = "Please agree to the terms"; false }
            else -> { errorMsg = ""; true }
        }
    }

    fun register() {
        val uid = auth.currentUser?.uid ?: return
        val phone = auth.currentUser?.phoneNumber ?: ""
        isLoading = true
        errorMsg = ""

        try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    val geoPoint = if (location != null) {
                        GeoPoint(location.latitude, location.longitude)
                    } else {
                        GeoPoint(12.9716, 77.5946)
                    }

                    val donorId = "RS-BLR-${System.currentTimeMillis() % 10000}"
                    val donor = hashMapOf(
                        "name" to name.trim(),
                        "phone" to phone,
                        "bloodGroup" to selectedBloodGroup,
                        "dateOfBirth" to dob,
                        "city" to city.trim(),
                        "taluka" to taluka.trim(),
                        "isAvailable" to true,
                        "aadhaarVerified" to false,
                        "fcmToken" to "",
                        "totalDonations" to 0,
                        "trustScore" to 80,
                        "donorId" to donorId,
                        "lastDonationDateMillis" to 0L,
                        "hemoglobin" to 0.0,
                        "createdAtMillis" to System.currentTimeMillis(),
                        "location" to geoPoint
                    )

                    db.collection("donors").document(uid)
                        .set(donor)
                        .addOnSuccessListener {
                            FirebaseMessaging.getInstance().token
                                .addOnSuccessListener { token ->
                                    if (token.isNotEmpty()) {
                                        db.collection("donors").document(uid)
                                            .update("fcmToken", token)
                                    }
                                }
                            isLoading = false
                            onRegistrationComplete()
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMsg = e.message ?: "Registration failed. Try again."
                        }
                }
                .addOnFailureListener {
                    // GPS failed — use fallback location
                    val donorId = "RS-BLR-${System.currentTimeMillis() % 10000}"
                    val donor = hashMapOf(
                        "name" to name.trim(),
                        "phone" to phone,
                        "bloodGroup" to selectedBloodGroup,
                        "dateOfBirth" to dob,
                        "city" to city.trim(),
                        "taluka" to taluka.trim(),
                        "isAvailable" to true,
                        "aadhaarVerified" to false,
                        "fcmToken" to "",
                        "totalDonations" to 0,
                        "trustScore" to 80,
                        "donorId" to donorId,
                        "lastDonationDateMillis" to 0L,
                        "hemoglobin" to 0.0,
                        "createdAtMillis" to System.currentTimeMillis(),
                        "location" to GeoPoint(12.9716, 77.5946)
                    )
                    db.collection("donors").document(uid)
                        .set(donor)
                        .addOnSuccessListener {
                            FirebaseMessaging.getInstance().token
                                .addOnSuccessListener { token ->
                                    if (token.isNotEmpty()) {
                                        db.collection("donors").document(uid)
                                            .update("fcmToken", token)
                                    }
                                }
                            isLoading = false
                            onRegistrationComplete()
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMsg = e.message ?: "Registration failed."
                        }
                }
        } catch (e: Exception) {
            isLoading = false
            errorMsg = "Location error. Please grant location permission."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(RedSurface, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🩸", fontSize = 32.sp) }

            Spacer(Modifier.height(16.dp))
            Text("Create Donor Profile",
                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Step $currentStep of 2",
                fontSize = 13.sp, color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f).height(4.dp)
                        .background(RedPrimary, RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f).height(4.dp)
                        .background(
                            if (currentStep == 2) RedPrimary else BorderSubtle,
                            RoundedCornerShape(2.dp)
                        )
                )
            }

            Spacer(Modifier.height(24.dp))

            AnimatedContent(targetState = currentStep, label = "step") { step ->
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (step == 1) {
                        RegisterField("Full Name", name,
                            "Enter your full name") { name = it }
                        RegisterField("Date of Birth", dob, "DD/MM/YYYY",
                            onValueChange = {
                                if (it.length <= 10) {
                                    val clean = it.filter { c -> c.isDigit() }
                                    dob = when {
                                        clean.length <= 2 -> clean
                                        clean.length <= 4 ->
                                            "${clean.substring(0,2)}/${clean.substring(2)}"
                                        else ->
                                            "${clean.substring(0,2)}/${clean.substring(2,4)}/${
                                                clean.substring(4, minOf(clean.length, 8))}"
                                    }
                                }
                            }
                        )
                        RegisterField("City", city, "e.g. Bengaluru") { city = it }
                        RegisterField("Taluka (Optional)", taluka,
                            "e.g. JP Nagar") { taluka = it }
                    } else {
                        Text("Blood Group", fontSize = 12.sp,
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

                        RegisterField("Weight (kg)", weight, "Must be 50kg or above",
                            onValueChange = {
                                if (it.length <= 3) weight = it.filter { c -> c.isDigit() }
                            }
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { agreedToTerms = !agreedToTerms }
                                .background(BgCard, RoundedCornerShape(12.dp))
                                .border(0.5.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Checkbox(
                                checked = agreedToTerms,
                                onCheckedChange = { agreedToTerms = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = RedPrimary,
                                    uncheckedColor = TextTertiary
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "I confirm I am 18-65 years old, weigh above 50kg, and am fit to donate blood.",
                                fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RedSurface, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(errorMsg, color = RedLight, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (currentStep == 1) {
                        if (validateStep1()) currentStep = 2
                    } else {
                        if (validateStep2()) register()
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedDark,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (currentStep == 1) "Next →" else "Create Profile",
                        fontWeight = FontWeight.Bold, fontSize = 15.sp
                    )
                }
            }

            if (currentStep == 2) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { currentStep = 1; errorMsg = "" }) {
                    Text("← Back", color = TextSecondary)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun RegisterField(
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