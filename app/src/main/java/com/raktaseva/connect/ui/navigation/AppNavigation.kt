package com.raktaseva.connect.ui.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.raktaseva.connect.ui.alert.AlertDetailScreen
import com.raktaseva.connect.ui.auth.LoginScreen
import com.raktaseva.connect.ui.chat.ChatScreen
import com.raktaseva.connect.ui.donorId.DonorIdScreen
import com.raktaseva.connect.ui.home.HomeScreen
import com.raktaseva.connect.ui.profile.ProfileScreen
import com.raktaseva.connect.ui.register.RegisterScreen
import com.raktaseva.connect.ui.request.CreateRequestScreen

sealed class Screen(val route: String) {
    object Login         : Screen("login")
    object Register      : Screen("register")
    object Home          : Screen("home")
    object Chat          : Screen("chat")
    object DonorId       : Screen("donor_id")
    object Profile       : Screen("profile")
    object CreateRequest : Screen("create_request")
    object AlertDetail   : Screen("alert/{requestId}") {
        fun createRoute(id: String) = "alert/$id"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }) + fadeIn()
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { isNewUser ->
                    if (isNewUser) {
                        navController.navigate(Screen.Register.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegistrationComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onAlertClick = { id ->
                    navController.navigate(Screen.AlertDetail.createRoute(id))
                },
                onChatClick = { navController.navigate(Screen.Chat.route) },
                onDonorIdClick = { navController.navigate(Screen.DonorId.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onCreateRequestClick = { navController.navigate(Screen.CreateRequest.route) }
            )
        }
        composable(Screen.Chat.route) {
            ChatScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.DonorId.route) {
            DonorIdScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.CreateRequest.route) {
            CreateRequestScreen(
                onBack = { navController.popBackStack() },
                onRequestCreated = { navController.popBackStack() }
            )
        }
        composable(Screen.AlertDetail.route) { backStack ->
            val requestId = backStack.arguments?.getString("requestId") ?: ""
            AlertDetailScreen(
                requestId = requestId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}