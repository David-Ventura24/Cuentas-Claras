package com.example.cuentas_clarasapp.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cuentas_clarasapp.navigation.Routes

private val BgDark  = Color(0xFF111013)
private val BgCard  = Color(0xFF1A1820)
private val Purple  = Color(0xFF985EFF)
private val TextMuted = Color(0x66FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(color = Purple)
                }
                is ProfileUiState.Error -> {
                    Text((uiState as ProfileUiState.Error).mensaje, color = Color.Red, fontSize = 16.sp)
                }
                is ProfileUiState.Success -> {
                    val usuario = (uiState as ProfileUiState.Success)

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier.size(96.dp).clip(CircleShape).background(Purple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(usuario.nombre.take(1), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(usuario.nombre, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Text(usuario.carrera, color = TextMuted, fontSize = 16.sp)
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(BgCard).padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Moneda Principal", color = TextMuted, fontSize = 15.sp)
                                Text(usuario.moneda, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estado de Cuenta", color = TextMuted, fontSize = 15.sp)
                                Text(usuario.estadoCuenta, color = Color(0xFF4CAF50), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.cerrarSesion {
                                    navController.navigate(Routes.Login) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252).copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(54.dp)
                        ) {
                            Text("Cerrar Sesión", color = Color(0xFFFF5252), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
