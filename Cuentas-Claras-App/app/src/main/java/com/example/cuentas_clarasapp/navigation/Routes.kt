package com.example.cuentas_clarasapp.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes {
    @Serializable
    data object Splash : Routes()

    @Serializable
    data object Login : Routes()

    @Serializable
    data object Register : Routes()

    @Serializable
    data object Home : Routes()

    @Serializable
    data object Budget : Routes()

    @Serializable
    data object AddExpense : Routes()

    @Serializable
    data object Analytics : Routes()

    @Serializable
    data object History : Routes()

    @Serializable
    data object Notifications : Routes()

    @Serializable
    data object Profile : Routes()
}