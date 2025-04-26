package com.tfg.umeegunero.feature.common.splash.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tfg.umeegunero.R
import androidx.compose.ui.graphics.graphicsLayer
import com.tfg.umeegunero.ui.theme.CentroColor
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.FamiliarColor
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * SplashScreen profesional y moderna para UmeEgunero.
 * Utiliza Material3, gradiente pastel y animaciones sutiles.
 * Es la primera impresión de la app, cuidando la estética y la experiencia.
 *
 * @param onSplashComplete Acción a ejecutar al finalizar la animación de splash.
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.92f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "Scale Animation"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "Alpha Animation"
    )
    // Animación del gradiente de fondo (desplazamiento vertical sutil)
    val gradientOffset by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 2200, easing = LinearEasing),
        label = "Gradient Offset"
    )
    // Animación de escala y opacidad para el logo
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.7f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "Logo Scale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "Logo Alpha"
    )
    // Tonos pastel para el gradiente de fondo
    val CentroColorPastel = CentroColor.copy(alpha = 0.22f)
    val ProfesorColorPastel = ProfesorColor.copy(alpha = 0.18f)
    val FamiliarColorPastel = FamiliarColor.copy(alpha = 0.18f)

    // Determinar si el fondo es claro para ajustar el color del texto
    val isBackgroundLight = true // Puedes mejorar esto con una función si el gradiente cambia dinámicamente
    val textColor = if (isBackgroundLight) Color(0xFF222222) else Color.White

    LaunchedEffect(key1 = true) {
        startAnimation = true
        kotlinx.coroutines.delay(2200)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CentroColorPastel,
                        ProfesorColorPastel,
                        FamiliarColorPastel
                    ),
                    startY = 0f + 200f * gradientOffset,
                    endY = 1200f + 200f * gradientOffset
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Onda decorativa inferior para un toque moderno
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .graphicsLayer(alpha = 0.7f)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            FamiliarColorPastel,
                            CentroColorPastel,
                            ProfesorColorPastel
                        )
                    ),
                    shape = MaterialTheme.shapes.extraLarge.copy(bottomStart = androidx.compose.foundation.shape.CornerSize(80.dp), bottomEnd = androidx.compose.foundation.shape.CornerSize(80.dp))
                )
        )
        // Contenido principal: logo animado y texto con fade-in
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Logo grande, animado y centrado
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = Color.White.copy(alpha = 0.18f),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .size(180.dp)
                    .graphicsLayer(
                        scaleX = logoScale,
                        scaleY = logoScale,
                        alpha = logoAlpha
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_icon),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            // Texto y créditos siempre visibles junto al logo
            Surface(
                color = Color.White.copy(alpha = 0.65f),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 0.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "UmeEgunero",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "v1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Diseñado y creado por Maitane Ibañez Irazabal",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
} 