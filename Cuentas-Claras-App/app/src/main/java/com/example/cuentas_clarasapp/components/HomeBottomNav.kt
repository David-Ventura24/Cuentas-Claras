package com.example.cuentas_clarasapp.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Purple = Color(0xFF985EFF)
private val BgNav = Color(0xFF111013)
private val InactiveColor = Color(0x40FFFFFF)

data class NavigationItem(val title: String, val icon: ImageVector)

@Composable
fun CuentasClarasBottomNav(
    currentDestinationIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf(
        NavigationItem("Inicio", Icons.Default.Home),
        NavigationItem("Historial", Icons.Default.List),
        NavigationItem("Gráficas", Icons.Default.DateRange),
        NavigationItem("Presupuesto", Icons.Default.DateRange)
    )

    // NavigationBar nativo: el Scaffold SÍ mide esto automáticamente
    // y lo incluye en el innerPadding que reciben las pantallas hijas.
    NavigationBar(
        containerColor = BgNav,
        tonalElevation = 0.dp,
        modifier = Modifier.height(64.dp)
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = currentDestinationIndex == index

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(item.title, fontSize = 10.sp)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Purple,
                    selectedTextColor = Purple,
                    unselectedIconColor = InactiveColor,
                    unselectedTextColor = InactiveColor,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}