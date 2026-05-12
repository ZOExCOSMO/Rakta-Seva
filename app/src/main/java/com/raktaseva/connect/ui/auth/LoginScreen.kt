package com.raktaseva.connect.ui.auth

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.raktaseva.connect.ui.theme.*
import java.util.concurrent.TimeUnit

@Composable
fun LoginScreen(onLoginSuccess: (isNewUser: Boolean) -> Unit) {
    val context  = LocalContext.current
    val activity = context as Activity
    val auth     = FirebaseAuth.getInstance()
    val db       = FirebaseFirestore.getInstance()

    var phone          by remember { mutableStateOf("") }
    var otp            by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf("") }
    var otpSent        by remember { mutableStateOf(false) }
    var isLoading      by remember { mutableStateOf(false) }
    var errorMsg       by remember { mutableStateOf("") }

    fun checkIfRegistered(uid: String) {
        db.collection("donors").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists() && doc.getString("name") != null) {
                    onLoginSuccess(false) // existing user → Home
                } else {
                    onLoginSuccess(true) // new user → Register
                }
            }
            .addOnFailureListener {
                onLoginSuccess(true) // on error assume new user
            }
    }

    fun sendOtp() {
        if (phone.length != 10) {
            errorMsg = "Enter a valid 10-digit number"
            return
        }
        isLoading = true
        errorMsg = ""
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phone")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { result ->
                            isLoading = false
                            val uid = result.user?.uid ?: return@addOnSuccessListener
                            checkIfRegistered(uid)
                        }
                        .addOnFailureListener {
                            isLoading = false
                            errorMsg = "Auto-verification failed"
                        }
                }
                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    errorMsg = e.message ?: "Verification failed"
                    isLoading = false
                }
                override fun onCodeSent(
                    vId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = vId
                    otpSent   = true
                    isLoading = false
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp() {
        if (otp.length != 6) {
            errorMsg = "Enter the 6-digit OTP"
            return
        }
        isLoading = true
        errorMsg = ""
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                isLoading = false
                val uid = result.user?.uid ?: return@addOnSuccessListener
                checkIfRegistered(uid)
            }
            .addOnFailureListener {
                isLoading = false
                errorMsg = "Invalid OTP. Try again."
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(RedSurface, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🩸", fontSize = 36.sp) }

            Spacer(Modifier.height(20.dp))

            Text("Rakta-Seva Connect",
                fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Saving lives, one alert at a time",
                fontSize = 14.sp, color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp))

            Spacer(Modifier.height(40.dp))

            // Phone input
            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10) phone = it },
                label = { Text("Mobile Number", color = TextSecondary) },
                prefix = { Text("+91  ", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !otpSent,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor    = RedPrimary,
                    unfocusedBorderColor  = BorderSubtle,
                    focusedTextColor      = TextPrimary,
                    unfocusedTextColor    = TextPrimary,
                    disabledTextColor     = TextSecondary,
                    disabledBorderColor   = BorderSubtle,
                    cursorColor           = RedPrimary,
                    focusedContainerColor = BgCard,
                    unfocusedContainerColor = BgCard,
                    disabledContainerColor  = BgCard,
                )
            )

            // OTP input
            AnimatedVisibility(visible = otpSent) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) otp = it },
                        label = { Text("OTP", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor    = RedPrimary,
                            unfocusedBorderColor  = BorderSubtle,
                            focusedTextColor      = TextPrimary,
                            unfocusedTextColor    = TextPrimary,
                            cursorColor           = RedPrimary,
                            focusedContainerColor = BgCard,
                            unfocusedContainerColor = BgCard,
                        )
                    )
                }
            }

            if (errorMsg.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg, color = RedLight, fontSize = 13.sp,
                    textAlign = TextAlign.Center)
            }

            if (otpSent && !isLoading) {
                Spacer(Modifier.height(6.dp))
                Text("OTP sent to +91 $phone",
                    color = GreenLight, fontSize = 12.sp)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { if (otpSent) verifyOtp() else sendOtp() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedDark,
                    contentColor   = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color    = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (otpSent) "Verify OTP" else "Send OTP",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp
                    )
                }
            }

            if (otpSent) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = {
                    otpSent = false
                    otp = ""
                    errorMsg = ""
                }) {
                    Text("Change number", color = TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}