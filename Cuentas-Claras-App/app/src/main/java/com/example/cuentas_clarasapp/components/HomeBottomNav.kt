package com.example.cuentas_clarasapp.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.cuentas_clarasapp.navigation.Routes

private val Purple = Color(0xFF985EFF)
private val BgDark = Color(0xFF111013)

@Composable
fun CuentasClarasBottomNav(navController: NavController) {
    // 1. ESCUCHA REAL: Detecta la ruta activa en el NavHost automáticamente
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Obtenemos el destino actual (soporta el enfoque Type-Safe moderno de Jetpack Navigation)
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = BgDark,
        tonalElevation = 0.dp,
        modifier = Modifier.height(64.dp)
    ) {
        // Mapeamos las pestañas con sus rutas exactas del sistema Type-Safe
        // Nota: Asegúrate de que apunten a la estructura de tu clase 'Routes'
        val items = listOf(
            Triple("Inicio",      Icons.Outlined.Home,      Routes.Home),
            Triple("Historial",   Icons.Outlined.List,      Routes.History),
            Triple("Gráficas",    Icons.Outlined.Info,      Routes.AddExpense),
            Triple("Presupuesto", Icons.Outlined.DateRange, Routes.Budget),
        )

        items.forEach { (label, icon, route) ->
            // En Navigation Type-Safe, el route del destino contiene el nombre calificado de la clase
            val routeClassName = route::class.qualifiedName ?: ""
            val esSeleccionado = currentRoute?.contains(routeClassName) == true

            NavigationBarItem(
                selected = esSeleccionado,
                onClick = {
                    if (!esSeleccionado) {
                        navController.navigate(route) {
                            popUpTo(Routes.Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp)) },
                label = { Text(label, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    // MORADO EXCLUSIVO si está seleccionado, GRIS ATENUADO si no lo está
                    selectedIconColor   = Purple,
                    selectedTextColor   = Purple,
                    unselectedIconColor = Color.White.copy(alpha = 0.25f),
                    unselectedTextColor = Color.White.copy(alpha = 0.25f),
                    indicatorColor      = Color.Transparent // Quita la molesta píldora de fondo de Material 3
                )
            )
        }
    }
}