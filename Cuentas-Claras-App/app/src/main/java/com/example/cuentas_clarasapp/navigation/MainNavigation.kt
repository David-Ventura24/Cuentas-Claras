package com.example.cuentas_clarasapp.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cuentas_clarasapp.screens.auth.SplashScreen
import com.example.cuentas_clarasapp.screens.auth.LoginScreen
import com.example.cuentas_clarasapp.screens.auth.RegisterScreen
import com.example.cuentas_clarasapp.screens.home.HomeScreen
import com.example.cuentas_clarasapp.screens.budget.BudgetSetupScreen
import com.example.cuentas_clarasapp.screens.budget.BudgetSetupViewModel
import com.example.cuentas_clarasapp.screens.expense.AddExpenseScreen
import com.example.cuentas_clarasapp.screens.expense.AddExpenseViewModel
import com.example.cuentas_clarasapp.screens.history.HistoryScreen
import com.example.cuentas_clarasapp.screens.history.HistoryViewModel

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Splash
    ) {
        // --- FLUJO DE AUTENTICACIÓN ---
        composable<Routes.Splash> {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Login> {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.Register)
                },
                onNavigateToForgotPassword = {
                    // TODO: BACKEND INTEGRATION - Agregar flujo de recuperación de credenciales vía Supabase Auth
                },
                onLoginSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Register> {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Register) { inclusive = true }
                    }
                }
            )
        }

        // --- FLUJO PRINCIPAL DE LA APLICACIÓN ---
        composable<Routes.Home> {
            // El ViewModel se inicializa automáticamente por defecto dentro del HomeScreen,
            // manteniendo el grafo de navegación limpio y desacoplado.
            HomeScreen(navController = navController)
        }

        // --- FLUJO DE CONFIGURACIÓN FINANCIERA ---
        composable<Routes.Budget> {
            // Inicialización explícita del ciclo de vida del ViewModel para la configuración del presupuesto
            val budgetSetupViewModel: BudgetSetupViewModel = viewModel()

            BudgetSetupScreen(
                navController = navController,
                viewModel = budgetSetupViewModel
            )
        }
        composable<Routes.AddExpense> {
            val addExpenseViewModel: AddExpenseViewModel = viewModel()
            AddExpenseScreen(
                navController = navController,
                viewModel = addExpenseViewModel
            )
        }
        composable<Routes.History> {
            val historyViewModel: HistoryViewModel = viewModel()
            HistoryScreen(
                navController = navController,
                viewModel = historyViewModel
            )
        }
    }
}

@Composable
fun PlaceholderScreen(titulo: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = titulo, color = Color.White)
    }
}