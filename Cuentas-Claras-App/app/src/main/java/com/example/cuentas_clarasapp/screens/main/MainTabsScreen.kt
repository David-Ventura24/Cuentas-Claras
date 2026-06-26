package com.example.cuentas_clarasapp.screens.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cuentas_clarasapp.components.AutoHideFab
import com.example.cuentas_clarasapp.components.CuentasClarasBottomNav
import com.example.cuentas_clarasapp.navigation.Routes
import com.example.cuentas_clarasapp.screens.home.HomeScreen
import com.example.cuentas_clarasapp.screens.home.HomeViewModel
import com.example.cuentas_clarasapp.screens.history.HistoryScreen
import com.example.cuentas_clarasapp.screens.history.HistoryViewModel
import com.example.cuentas_clarasapp.screens.analytics.AnalyticsScreen
import com.example.cuentas_clarasapp.screens.analytics.AnalyticsViewModel
import com.example.cuentas_clarasapp.screens.budget.BudgetScreen
import androidx.compose.ui.platform.LocalContext
import com.example.cuentas_clarasapp.data.local.AppDatabase
import com.example.cuentas_clarasapp.data.repositories.FinanzasRepository
import kotlinx.coroutines.launch
import kotlin.math.abs

private val Purple = Color(0xFF985EFF)
private val BgDark = Color(0xFF111013)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainTabsScreen(
    navController: NavController,
    homeViewModel: HomeViewModel
) {
    val tabsCount = 4
    val pagerState = rememberPagerState(pageCount = { tabsCount })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val database = AppDatabase.getDatabase(context)
    val repository = FinanzasRepository(database.gastoDao(), database.ahorroDao())

    // Controla si el FAB está visible, actualizado desde HomeScreen según la dirección del scroll
    var fabVisible by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = BgDark,
        floatingActionButton = {
            if (pagerState.currentPage == 0) {
                AutoHideFab(
                    visible = fabVisible,
                    icon = Icons.Default.Add,
                    contentDescription = "Agregar Gasto",
                    containerColor = Purple,
                    contentColor = Color.White,
                    onClick = { navController.navigate(Routes.AddExpense) },
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            CuentasClarasBottomNav(
                currentDestinationIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch {
                        if (abs(pagerState.currentPage - index) > 1) {
                            pagerState.scrollToPage(index)
                        } else {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> HomeScreen(
                        navController = navController,
                        viewModel = homeViewModel,
                        paddingValues = innerPadding,
                        onFabVisibilityChange = { visible -> fabVisible = visible }
                    )
                    1 -> {
                        val historyViewModel: HistoryViewModel = viewModel(
                            factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return HistoryViewModel(repository) as T
                                }
                            }
                        )
                        HistoryScreen(
                            navController = navController,
                            viewModel = historyViewModel,
                            paddingValues = innerPadding
                        )
                    }
                    2 -> {
                        val analyticsViewModel: AnalyticsViewModel = viewModel(
                            factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    return AnalyticsViewModel(repository) as T
                                }
                            }
                        )
                        AnalyticsScreen(
                            navController = navController,
                            viewModel = analyticsViewModel,
                            paddingValues = innerPadding
                        )
                    }
                    3 -> {
                        BudgetScreen(
                            navController = navController,
                            homeViewModel = homeViewModel,
                            paddingValues = innerPadding
                        )
                    }
                }
            }
        }
    }
}