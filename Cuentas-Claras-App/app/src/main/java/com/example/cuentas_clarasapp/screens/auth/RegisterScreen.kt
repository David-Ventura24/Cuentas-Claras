package com.example.cuentas_clarasapp.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

private val Purple      = Color(0xFF985EFF)
private val BgDark      = Color(0xFF111013)
private val BgCard      = Color(0x0AFFFFFF)
private val BorderDefault = Color(0x17FFFFFF)
private val BorderFocused = Color(0x73985EFF)
private val TextMuted   = Color(0x47FFFFFF)

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var confirm by remember { mutableStateOf("") }
    var confirmVisible by remember { mutableStateOf(false) }

    val passwordMismatch = confirm.isNotEmpty() && uiState.password != confirm

    val alpha  = remember { Animatable(0f) }
    val slideY = remember { Animatable(20f) }
    val scope  = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch { alpha.animateTo(1f,  tween(450, easing = EaseOutCubic)) }
        scope.launch { slideY.animateTo(0f, tween(450, easing = EaseOutCubic)) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(vertical = 48.dp)
                .alpha(alpha.value)
                .offset(y = slideY.value.dp),
            horizontalAlignment = Alignment.Start
        ) {

            // --- Encabezado ---
            Text(
                text = "Crear cuenta",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Completa tus datos para comenzar",
                color = TextMuted,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // --- Error Message ---
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // --- Nombre ---
            RegFieldLabel("Nombre")
            Spacer(modifier = Modifier.height(7.dp))
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                placeholder = { Text("Tu nombre completo", color = Color(0x2EFFFFFF), fontSize = 15.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = regFieldColors()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // --- Correo ---
            RegFieldLabel("Correo")
            Spacer(modifier = Modifier.height(7.dp))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                placeholder = { Text("correo@ejemplo.com", color = Color(0x2EFFFFFF), fontSize = 15.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = regFieldColors()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // --- Contraseña ---
            RegFieldLabel("Contraseña")
            Spacer(modifier = Modifier.height(7.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                placeholder = { Text("Mínimo 8 caracteres", color = Color(0x2EFFFFFF), fontSize = 15.sp) },
                singleLine = true,
                visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { viewModel.onTogglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (uiState.passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0x33FFFFFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = regFieldColors()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // --- Confirmar contraseña ---
            RegFieldLabel("Confirmar contraseña")
            Spacer(modifier = Modifier.height(7.dp))
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                placeholder = { Text("Repite tu contraseña", color = Color(0x2EFFFFFF), fontSize = 15.sp) },
                singleLine = true,
                isError = passwordMismatch,
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(
                            imageVector = if (confirmVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0x33FFFFFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                supportingText = {
                    if (passwordMismatch) {
                        Text(
                            text = "Las contraseñas no coinciden",
                            color = Color(0xFFFF6B6B),
                            fontSize = 12.sp
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = regFieldColors(isError = passwordMismatch)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // --- Botón registrarse ---
            val formValid = uiState.name.isNotBlank()
                    && uiState.email.isNotBlank()
                    && uiState.password.length >= 8
                    && uiState.password == confirm

            Button(
                onClick = { viewModel.register(onRegisterSuccess) },
                enabled = formValid && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple,
                    contentColor = Color.White,
                    disabledContainerColor = Purple.copy(alpha = 0.30f),
                    disabledContentColor = Color.White.copy(alpha = 0.40f)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Crear cuenta", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Ya tengo cuenta ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("¿Ya tienes cuenta? ", color = TextMuted, fontSize = 13.sp)
                Text(
                    text = "Inicia sesión",
                    color = Purple.copy(alpha = 0.70f),
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@Composable
private fun RegFieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = Color(0x4DFFFFFF),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun regFieldColors(isError: Boolean = false) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = BgCard,
    unfocusedContainerColor = BgCard,
    focusedBorderColor = if (isError) Color(0xFFFF6B6B) else BorderFocused,
    unfocusedBorderColor = if (isError) Color(0x80FF6B6B) else BorderDefault,
    errorBorderColor = Color(0xFFFF6B6B),
    cursorColor = Purple
)
