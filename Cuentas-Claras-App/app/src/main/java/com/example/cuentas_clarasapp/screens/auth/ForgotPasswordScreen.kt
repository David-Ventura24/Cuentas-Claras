package com.example.cuentas_clarasapp.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

private val Purple = Color(0xFF985EFF)
private val BgDark  = Color(0xFF111013)
private val BgCard  = Color(0x0AFFFFFF)
private val BorderDefault = Color(0x17FFFFFF)
private val BorderFocused = Color(0x73985EFF)
private val TextMuted = Color(0x47FFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val alpha = remember { Animatable(0f) }
    val slideY = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        scope.launch { alpha.animateTo(1f, tween(450, easing = EaseOutCubic)) }
        scope.launch { slideY.animateTo(0f, tween(450, easing = EaseOutCubic)) }
    }

    Scaffold(
        containerColor = BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgDark),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp)
                    .alpha(alpha.value)
                    .offset(y = slideY.value.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.step == 1) {
                    StepRequestEmail(uiState, viewModel)
                } else {
                    StepResetPassword(uiState, viewModel, onSuccess)
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = uiState.errorMessage!!, color = Color.Red, fontSize = 14.sp)
                }

                if (uiState.successMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = uiState.successMessage!!, color = Color.Green, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun StepRequestEmail(
    uiState: ForgotPasswordUiState,
    viewModel: ForgotPasswordViewModel
) {
    Text(
        text = "Ingresa tu correo electrónico y te enviaremos un código para restablecer tu contraseña.",
        color = TextMuted,
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(32.dp))

    FieldLabel("Correo")
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = uiState.email,
        onValueChange = { viewModel.onEmailChange(it) },
        placeholder = { Text("correo@ejemplo.com", color = Color(0x2EFFFFFF), fontSize = 15.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = fieldColors()
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = { viewModel.solicitarCodigo() },
        enabled = !uiState.isLoading && uiState.email.isNotBlank(),
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Purple)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
            Text("Enviar código", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StepResetPassword(
    uiState: ForgotPasswordUiState,
    viewModel: ForgotPasswordViewModel,
    onSuccess: () -> Unit
) {
    Text(
        text = "Ingresa el código de 6 dígitos enviado a ${uiState.email} y tu nueva contraseña.",
        color = TextMuted,
        fontSize = 14.sp
    )

    Spacer(modifier = Modifier.height(32.dp))

    FieldLabel("Código de verificación")
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = uiState.token,
        onValueChange = { viewModel.onTokenChange(it) },
        placeholder = { Text("123456", color = Color(0x2EFFFFFF), fontSize = 15.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = fieldColors()
    )

    Spacer(modifier = Modifier.height(18.dp))

    FieldLabel("Nueva contraseña")
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = uiState.newPassword,
        onValueChange = { viewModel.onNewPasswordChange(it) },
        placeholder = { Text("••••••••", color = Color(0x2EFFFFFF), fontSize = 15.sp) },
        singleLine = true,
        visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
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
        colors = fieldColors()
    )

    Spacer(modifier = Modifier.height(18.dp))

    FieldLabel("Confirmar contraseña")
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = uiState.confirmPassword,
        onValueChange = { viewModel.onConfirmPasswordChange(it) },
        placeholder = { Text("••••••••", color = Color(0x2EFFFFFF), fontSize = 15.sp) },
        singleLine = true,
        visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = fieldColors()
    )

    Spacer(modifier = Modifier.height(32.dp))

    Button(
        onClick = { viewModel.restablecerContrasena(onSuccess) },
        enabled = !uiState.isLoading && uiState.token.length == 6 && uiState.newPassword.length >= 8,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Purple)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
            Text("Restablecer contraseña", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = Color(0x4DFFFFFF),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.8.sp
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = BgCard,
    unfocusedContainerColor = BgCard,
    focusedBorderColor = BorderFocused,
    unfocusedBorderColor = BorderDefault,
    cursorColor = Purple
)
