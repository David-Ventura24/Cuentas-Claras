package com.example.cuentas_clarasapp.screens.notifications

enum class NotificationPriority {
    INFO, WARNING, CRITICAL
}

data class AppNotification(
    val id: String,
    val titulo: String,
    val mensaje: String,
    val fecha: String,
    val prioridad: NotificationPriority
)