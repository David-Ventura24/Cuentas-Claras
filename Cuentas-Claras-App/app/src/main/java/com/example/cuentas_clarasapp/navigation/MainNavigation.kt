package com.example.cuentas_clarasapp.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cuentas_clarasapp.data.local.SessionManager
import com.example.cuentas_clarasapp.screens.auth.SplashScreen
import com.example.cuentas_clarasapp.screens.auth.LoginScreen
import com.example.cuentas_clarasapp.screens.auth.RegisterScreen
import com.example.cuentas_clarasapp.screens.home.HomeViewModel
import com.example.cuentas_clarasapp.screens.history.HistoryScreen
import com.example.cuentas_clarasapp.screens.history.HistoryViewModel
import com.example.cuentas_clarasapp.screens.analytics.AnalyticsScreen
import com.example.cuentas_clarasapp.screens.analytics.AnalyticsViewModel
import com.example.cuentas_clarasapp.screens.expense.AddExpenseScreen
import com.example.cuentas_clarasapp.screens.expense.AddExpenseViewModel
import com.example.cuentas_clarasapp.screens.main.MainTabsScreen
import com.example.cuentas_clarasapp.screens.notifications.NotificationScreen
import com.example.cuentas_clarasapp.screens.notifications.NotificationViewModel
import com.example.cuentas_clarasapp.screens.profile.ProfileScreen
import com.example.cuentas_clarasapp.screens.profile.ProfileViewModel
import com.example.cuentas_clarasapp.screens.savings.GlobalSavingsScreen
import com.example.cuentas_clarasapp.screens.savings.SavingsViewModel

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val token = sessionManager.obtenerToken()

    // ViewModel compartido para el Home
    val sharedHomeViewModel: HomeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = if (token != null) Routes.Home else Routes.Splash,
        enterTransition = { slideInHorizontally(animationSpec = tween(400)) + fadeIn() },
        exitTransition = { slideOutHorizontally(animationSpec = tween(400)) + fadeOut() }
    ) {
        composable<Routes.Splash> {
            SplashScreen(onNavigateToLogin = {
                navController.navigate(Routes.Login) { popUpTo(Routes.Splash) { inclusive = true } }
            })
        }

        composable<Routes.Login> {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.Register) },
                onNavigateToForgotPassword = { navController.navigate(Routes.ForgotPassword) },
                onLoginSuccess = {
                    navController.navigate(Routes.Home) { popUpTo(Routes.Login) { inclusive = true } }
                }
            )
        }

        composable<Routes.ForgotPassword> {
            com.example.cuentas_clarasapp.screens.auth.ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.ForgotPassword) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Register> {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.Login) { popUpTo(Routes.Register) { inclusive = true } }
                }
            )
        }

        composable<Routes.Home> {
            MainTabsScreen(navController = navController, homeViewModel = sharedHomeViewModel)
        }

        composable<Routes.Notifications> {
            NotificationScreen(navController = navController, notificationViewModel = viewModel(), homeViewModel = sharedHomeViewModel)
        }

        composable<Routes.Profile> {
            ProfileScreen(navController = navController, viewModel = viewModel())
        }

        composable<Routes.AddExpense> {
            AddExpenseScreen(navController = navController, homeViewModel = sharedHomeViewModel, viewModel = viewModel())
        }

        composable<Routes.Analytics> {
            AnalyticsScreen(navController = navController, viewModel = viewModel())
        }

        composable<Routes.History> {
            HistoryScreen(navController = navController, viewModel = viewModel())
        }

        composable<Routes.GlobalSavings> {
            GlobalSavingsScreen(navController = navController, viewModel = viewModel())
        }
    }
}