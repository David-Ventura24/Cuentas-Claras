package com.example.cuentas_clarasapp.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint // ➔ NUEVO IMPORT
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // ➔ NUEVO IMPORT
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity // ➔ NUEVO IMPORT
import com.example.cuentas_clarasapp.utils.BiometricHelper // ➔ NUEVO IMPORT
import kotlinx.coroutines.launch

private val Purple = Color(0xFF985EFF)
private val BgDark  = Color(0xFF111013)
private val BgCard  = Color(0x0AFFFFFF)
private val BorderDefault = Color(0x17FFFFFF)
private val BorderFocused = Color(0x73985EFF)
private val TextMuted = Color(0x47FFFFFF)

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Estados para control de Biometría 🌟
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var errorBiometrico by remember { mutableStateOf("") }
    val mostrarBotonBiometrico = remember { BiometricHelper.esBiometriaDisponible(context) }

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
                .padding(horizontal = 28.dp)
                .alpha(alpha.value)
                .offset(y = slideY.value.dp),
            horizontalAlignment = Alignment.Start
        ) {

            Text(
                text = "Iniciar sesión",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Ingresa tus datos para continuar",
                color = TextMuted,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // --- Correo ---
            FieldLabel("Correo")
            Spacer(modifier = Modifier.height(7.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("correo@ejemplo.com", color = Color(0x2EFFFFFF), fontSize = 15.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors()
            )

            Spacer(modifier = Modifier.height(18.dp))

            // --- Contraseña ---
            FieldLabel("Contraseña")
            Spacer(modifier = Modifier.height(7.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("••••••••", color = Color(0x2EFFFFFF), fontSize = 15.sp) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
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

            Spacer(modifier = Modifier.height(28.dp))

            // --- Botón principal ---
            Button(
                onClick = onLoginSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple,
                    contentColor = Color.White
                )
            ) {
                Text("Iniciar sesión", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- Botón Registrarme ---
            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderDefault),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0x73FFFFFF))
            ) {
                Text("Registrarme", fontSize = 15.sp, fontWeight = FontWeight.Normal)
            }

            // ==========================================
            // SECCIÓN BIOMÉTRICA (Huella / Face ID) 🌟
            // ==========================================
            if (mostrarBotonBiometrico && activity != null) {
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        BiometricHelper.iniciarAutenticacion(
                            activity = activity,
                            onSuccess = { result ->
                                errorBiometrico = ""
                                // Éxito nativo. Aquí tu compañero conectará las credenciales seguras
                                onLoginSuccess()
                            },
                            onError = { mensaje ->
                                errorBiometrico = mensaje
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Purple.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Purple)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Login Biométrico",
                        tint = Purple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Ingresar con huella o rostro", fontSize = 15.sp, fontWeight = FontWeight.Normal)
                }
            }

            // Mensaje de error de biometría (si existiera)
            if (errorBiometrico.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorBiometrico,
                    color = Color.Red.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Olvide contraseña ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("¿Olvidaste tu contraseña? ", color = TextMuted, fontSize = 13.sp)
                Text(
                    text = "Recupérala aquí",
                    color = Purple.copy(alpha = 0.70f),
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onNavigateToForgotPassword() }
                )
            }
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