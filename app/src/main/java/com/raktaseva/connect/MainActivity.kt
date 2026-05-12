package com.raktaseva.connect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.raktaseva.connect.ui.navigation.AppNavigation
import com.raktaseva.connect.ui.navigation.Screen
import com.raktaseva.connect.ui.theme.BgDark
import com.raktaseva.connect.ui.theme.RedPrimary
import com.raktaseva.connect.ui.theme.RaktaSevaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide system navigation bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Request notification permission Android 13+
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }

        val auth = FirebaseAuth.getInstance()
        val db   = FirebaseFirestore.getInstance()

        setContent {
            RaktaSevaTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    startDestination = when {
                        auth.currentUser == null -> Screen.Login.route
                        else -> {
                            val uid = auth.currentUser!!.uid
                            val doc = try {
                                db.collection("donors")
                                    .document(uid)
                                    .get()
                                    .await()
                            } catch (e: Exception) { null }

                            if (doc != null &&
                                doc.exists() &&
                                doc.getString("name") != null) {
                                Screen.Home.route
                            } else {
                                Screen.Register.route
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgDark
                ) {
                    if (startDestination == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(BgDark),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = RedPrimary)
                        }
                    } else {
                        val navController = rememberNavController()
                        AppNavigation(
                            navController = navController,
                            startDestination = startDestination!!
                        )
                    }
                }
            }
        }
    }
}