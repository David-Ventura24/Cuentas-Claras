package com.example.cuentas_clarasapp.screens.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuentas_clarasapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {

    val alphaLogo = remember { Animatable(0f) }
    val scaleLogo = remember { Animatable(0.75f) }
    val alphaText = remember { Animatable(0f) }
    val alphaTagline = remember { Animatable(0f) }
    val arcSweep = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    LaunchedEffect(Unit) {
        // Logo + anillo aparecen juntos, más rápido
        launch {
            alphaLogo.animateTo(1f, tween(350, easing = EaseOutCubic))
        }
        launch {
            scaleLogo.animateTo(1f, tween(400, easing = EaseOutCubic))
        }
        launch {
            arcSweep.animateTo(360f, tween(500, easing = EaseInOutCubic))
        }

        // Título y tagline aparecen casi enseguida después
        delay(350)
        alphaText.animateTo(1f, tween(250, easing = EaseOutCubic))
        alphaTagline.animateTo(1f, tween(250, easing = EaseOutCubic))

        // Pausa breve para que se aprecie, y navega
        delay(400)
        onNavigateToLogin()
    }

    val bgCenter = Color(0xFF1C1A2E)
    val bgEdge   = Color(0xFF0D0C14)
    val purple   = Color(0xFF985EFF)
    val purpleDim = Color(0xFF5B3899)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(bgCenter, bgEdge),
                    radius = 1100f
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Canvas(modifier = Modifier.size(280.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        purple.copy(alpha = 0.10f),
                        Color.Transparent
                    )
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 3.5.dp.toPx()
                    val inset = stroke / 2f
                    drawArc(
                        color = purpleDim.copy(alpha = 0.25f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(this.size.width - stroke, this.size.height - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (arcSweep.value >= 359f) ringAlpha else 1f)
                ) {
                    val stroke = 3.5.dp.toPx()
                    val inset = stroke / 2f
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                purple.copy(alpha = 0f),
                                purpleDim,
                                purple
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = arcSweep.value,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(this.size.width - stroke, this.size.height - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.cuentas_claras_logo),
                    contentDescription = "Logo de Cuentas Claras",
                    modifier = Modifier
                        .size(110.dp)
                        .alpha(alphaLogo.value)
                        .graphicsLayer(scaleX = scaleLogo.value, scaleY = scaleLogo.value)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Cuentas Claras",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.alpha(alphaText.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Control total, finanzas claras.",
                color = purple.copy(alpha = 0.80f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(alphaTagline.value)
                    .padding(horizontal = 32.dp)
            )
        }
    }
}