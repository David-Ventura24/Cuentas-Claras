package com.example.cuentas_clarasapp.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cuentas_clarasapp.screens.auth.SplashScreen
import com.example.cuentas_clarasapp.screens.auth.LoginScreen
import com.example.cuentas_clarasapp.screens.auth.RegisterScreen
import com.example.cuentas_clarasapp.screens.home.HomeScreen
import com.example.cuentas_clarasapp.screens.home.HomeViewModel
import com.example.cuentas_clarasapp.screens.home.HomeUiState
import com.example.cuentas_clarasapp.screens.budget.BudgetSetupScreen
import com.example.cuentas_clarasapp.screens.budget.BudgetSetupViewModel

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val sharedHomeViewModel: HomeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.Splash,
        // =========================================================================
        // CONFIGURACIÓN GLOBAL DE TRANSICIONES (Se recicla en todas las pantallas)
        // =========================================================================

        // 1. Cómo entra una nueva pantalla al frente (Desliza desde la derecha + aparece suave)
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) +
                    fadeIn(animationSpec = tween(400))
        },
        // 2. Cómo sale la pantalla anterior hacia atrás (Desliza hacia la izquierda + se desvanece)
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) +
                    fadeOut(animationSpec = tween(400))
        },
        // 3. Cómo vuelve a entrar una pantalla del historial al hacer atrás (Desliza desde la izquierda)
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) +
                    fadeIn(animationSpec = tween(400))
        },
        // 4. Cómo se destruye la pantalla actual al hacer atrás (Desliza hacia la derecha)
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) +
                    fadeOut(animationSpec = tween(400))
        }
    ) {
        // --- PANTALLA DE SPLASH ---
        composable<Routes.Splash> {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }

        // --- PANTALLA DE LOGIN ---
        composable<Routes.Login> {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.Register) },
                onNavigateToForgotPassword = { /* TODO */ },
                onLoginSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }
            )
        }

        // --- PANTALLA DE REGISTRO ---
        composable<Routes.Register> {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Register) { inclusive = true }
                    }
                }
            )
        }

        // --- PANTALLA PRINCIPAL (HOME) ---
        composable<Routes.Home> {
            HomeScreen(
                navController = navController,
                viewModel = sharedHomeViewModel
            )
        }

        // --- PANTALLA DE CONFIGURACIÓN DE PRESUPUESTO ---
        composable<Routes.Budget> {
            val budgetSetupViewModel: BudgetSetupViewModel = viewModel()
            val homeState = sharedHomeViewModel.uiState.collectAsState().value

            var saldo = 0f
            var periodo = "Sin configurar"
            var ahorro = 0f

            if (homeState is HomeUiState.Success) {
                saldo = homeState.data.saldoDisponible.toFloat()
                periodo = homeState.data.periodoPresupuesto
                ahorro = homeState.data.porcentajeAhorro.toFloat()
            }

            BudgetSetupScreen(
                navController = navController,
                viewModel = budgetSetupViewModel,
                saldoActualHome = saldo,
                periodoActualHome = periodo,
                ahorroActualHome = ahorro,
                onPresupuestoGuardado = { monto, per, ahr ->
                    sharedHomeViewModel.actualizarPresupuestoDesdeConfiguracion(
                        nuevoMonto = monto.toFloat(),
                        nuevoPeriodo = per,
                        nuevoPorcentajeAhorro = ahr.toFloat()
                    )
                }
            )
        }
    }
}
