package com.tfg.umeegunero.feature.common.welcome.screen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tfg.umeegunero.R
import com.tfg.umeegunero.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin
import com.tfg.umeegunero.feature.common.support.screen.TechnicalSupportScreen
import com.tfg.umeegunero.feature.common.support.screen.FAQScreen

// Extensión para verificar si el tema es claro
fun ColorScheme.isLight(): Boolean {
    val backgroundColor = this.background
    val luminance = (0.299 * backgroundColor.red + 0.587 * backgroundColor.green + 0.114 * backgroundColor.blue)
    return luminance > 0.5
}

/**
 * Tipos de usuario disponibles para la selección en la pantalla de bienvenida.
 * 
 * Esta enumeración define los diferentes perfiles de usuario que pueden acceder
 * al sistema, cada uno con sus propias funcionalidades y permisos.
 * 
 * @property ADMIN Administrador del sistema con acceso completo a todas las funcionalidades
 * @property CENTRO Personal administrativo de un centro educativo
 * @property PROFESOR Profesorado del centro educativo
 * @property FAMILIAR Padres, madres o tutores legales de los alumnos
 */
enum class WelcomeUserType {
    ADMIN,
    CENTRO,
    PROFESOR,
    FAMILIAR
}

/**
 * Pantalla de bienvenida y punto de entrada principal de la aplicación UmeEgunero.
 * 
 * Esta pantalla es la primera que ven los usuarios al abrir la aplicación. Proporciona
 * opciones para iniciar sesión según el tipo de usuario (administrador, centro, profesor
 * o familiar), registrarse como nuevo usuario, acceder a soporte técnico o cerrar la app.
 * 
 * Características visuales:
 * - Diseño atractivo con animaciones y efectos visuales
 * - Interfaz adaptativa para temas claro/oscuro
 * - Elementos decorativos dinámicos en el fondo
 * - Selección intuitiva del tipo de usuario
 * - Logo y branding distintivo de la aplicación
 * 
 * La pantalla implementa una navegación clara y directa hacia las principales funciones
 * de acceso al sistema, con un diseño que comunica la identidad de la aplicación como
 * herramienta educativa moderna y profesional.
 *
 * @param onNavigateToLogin Callback para navegar a la pantalla de login, con el tipo de usuario seleccionado
 * @param onNavigateToRegister Callback para navegar a la pantalla de registro
 * @param onCloseApp Callback para cerrar la aplicación
 * @param onDemoRequested Callback para solicitar una demostración (opcional)
 * @param onNavigateToSupport Callback para navegar al soporte general (opcional)
 * @param onNavigateToTechnicalSupport Callback para navegar al soporte técnico (opcional)
 * @param onNavigateToFAQ Callback para navegar a las preguntas frecuentes (opcional)
 */
@Composable
fun WelcomeScreen(
    onNavigateToLogin: (WelcomeUserType) -> Unit,
    onNavigateToRegister: () -> Unit = {},
    onCloseApp: () -> Unit,
    onDemoRequested: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToTechnicalSupport: () -> Unit = {},
    onNavigateToFAQ: () -> Unit = {}
) {
    val isLight = MaterialTheme.colorScheme.isLight()
    
    // Colores de fondo mejorados con gradientes más vibrantes
    val gradientColors = if (isLight) {
        listOf(
            Color(0xFF1E88E5).copy(alpha = 0.25f), // Azul vibrante
            Color(0xFF6A1B9A).copy(alpha = 0.15f), // Púrpura intenso
            Color(0xFF00695C).copy(alpha = 0.18f)  // Verde esmeralda
        )
    } else {
        listOf(
            Color(0xFF1A237E).copy(alpha = 0.9f),  // Azul oscuro más intenso
            Color(0xFF4A148C).copy(alpha = 0.7f),  // Púrpura oscuro
            Color(0xFF1B5E20).copy(alpha = 0.8f)   // Verde oscuro
        )
    }
    
    // Animación del fondo más notable
    val infiniteTransition = rememberInfiniteTransition(label = "Background Animation")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Background Offset"
    )
    
    // Animación adicional para elementos visuales
    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse Animation"
    )
    
    // Estado para la lista desplazable
    val scrollState = rememberLazyListState()
    
    // Efecto de desplazamiento paralaje
    val logoOffset by derivedStateOf {
        if (scrollState.firstVisibleItemIndex > 0) {
            0f
        } else {
            scrollState.firstVisibleItemScrollOffset * 0.5f
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(offset, 0f),
                    end = Offset(1f - offset, 1f)
                )
            )
    ) {
        // Extraer colores fuera del composable Canvas con colores más intensos
        val primaryColorTransparent = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        val secondaryColorTransparent = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        val tertiaryColorTransparent = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
        val primaryLineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        val secondaryLineColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        val tertiaryLineColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
        
        // Colores y valores para las ondas
        val waveColors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
        )
        
        // Elementos decorativos de fondo mejorados - usando un Canvas composable
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Círculos dinámicos con mayor tamaño y visibilidad
            repeat(25) { i ->
                val xPos = (canvasWidth * (i / 25f + offset * 0.6f)) % (canvasWidth * 1.2f) - 100f
                val yPos = (canvasHeight * ((i % 8) / 8f)) - 50f
                val radius = (30f + (i % 5) * 25f) * (if (i % 3 == 0) pulseAnimation * 0.7f else 1f)
                
                val circleColor = when (i % 3) {
                    0 -> primaryColorTransparent
                    1 -> secondaryColorTransparent
                    else -> tertiaryColorTransparent
                }
                
                drawCircle(
                    color = circleColor,
                    radius = radius,
                    center = Offset(xPos, yPos)
                )
            }
            
            // Líneas decorativas más visibles
            repeat(3) { i ->
                val lineY = canvasHeight * (0.2f + i * 0.25f)
                val lineColor = when (i) {
                    0 -> primaryLineColor
                    1 -> secondaryLineColor
                    else -> tertiaryLineColor
                }
                
                drawLine(
                    color = lineColor,
                    start = Offset(0f, lineY + offset * 50),
                    end = Offset(canvasWidth, lineY - offset * 30),
                    strokeWidth = (2 + i).dp.toPx()
                )
            }
            
            // Añadir ondas horizontales
            repeat(3) { i ->
                val startY = canvasHeight * (0.3f + i * 0.2f)
                val amplitude = 50f * (i + 1)
                val frequency = 0.02f * (i + 1)
                
                val path = Path()
                path.moveTo(0f, startY)
                
                for (x in 0..canvasWidth.toInt() step 5) {
                    val xFloat = x.toFloat()
                    val yOffset = amplitude * sin((xFloat * frequency) + offset * 5)
                    path.lineTo(xFloat, startY + yOffset)
                }
                
                drawPath(
                    path = path,
                    color = waveColors[i],
                    style = Stroke(
                        width = (1 + i).dp.toPx(),
                        pathEffect = PathEffect.cornerPathEffect(10f)
                    )
                )
            }
        }
        
        // Contenido principal desplazable
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 72.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo y título con efecto de paralaje mejorado - sin slogan
            item {
                Box(
                    modifier = Modifier
                        .padding(top = 32.dp, bottom = 16.dp)
                        .offset(y = (-logoOffset * 0.3f).dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Fondo circular con efecto de pulso
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .scale(pulseAnimation * 0.9f)
                    )
                    
                    // Título de la app con diseño elegante
                    Card(
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 16.dp)
                            .fillMaxWidth(0.85f)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = MaterialTheme.colorScheme.primary
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLight)
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                                .padding(vertical = 14.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Icono más grande y visible
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .shadow(
                                            elevation = 6.dp,
                                            shape = CircleShape
                                        )
                            .clip(CircleShape)
                                        .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_icon),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                            .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Texto más legible con sombra sutil
                    Text(
                        text = "UmeEgunero",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // Carrusel de onboarding
            item {
                OnboardingCarousel()
            }

            // Botones de acceso
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "ACCESO A LA PLATAFORMA",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .graphicsLayer(alpha = 0.9f)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .shadow(
                                elevation = 16.dp,
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLight)
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isLight) 4.dp else 6.dp
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = if (isLight) {
                                            listOf(
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                MaterialTheme.colorScheme.surface
                                            )
                                        } else {
                                            listOf(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                                            )
                                        }
                                    )
                                )
                        ) {
                            LoginButtons(
                                onCentroLogin = { onNavigateToLogin(WelcomeUserType.CENTRO) },
                                onProfesorLogin = { onNavigateToLogin(WelcomeUserType.PROFESOR) },
                                onFamiliarLogin = { onNavigateToLogin(WelcomeUserType.FAMILIAR) }
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 8.dp)
                            .height(1.dp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .shadow(
                                elevation = 8.dp,
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLight)
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isLight) 4.dp else 6.dp
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = if (isLight) {
                                            listOf(
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                MaterialTheme.colorScheme.surface
                                            )
                                        } else {
                                            listOf(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                                            )
                                        }
                                    )
                                )
                        ) {
                            Text(
                                text = "¿Eres familiar y no tienes cuenta?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                                modifier = Modifier.padding(bottom = 12.dp),
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = onNavigateToRegister,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Secondary
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 0.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HowToReg,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Regístrate",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
            
            // Botones de soporte y demo (ahora visibles con desplazamiento)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .shadow(
                            elevation = 8.dp,
                            spotColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLight)
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isLight) 4.dp else 6.dp
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (isLight) {
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
                                        )
                                    } else {
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                        )
                                    },
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                                )
                            )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                        Text(
                                    text = "Soporte y asistencia",
                            style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                                    textAlign = TextAlign.Start
                        )
                            }
                        
                            HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tiempo de respuesta estimado: < 24h",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            
                            // Botones de soporte técnico y FAQ
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToTechnicalSupport,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SupportAgent,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Soporte técnico",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                Button(
                                    onClick = onNavigateToFAQ,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Help,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "FAQ",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Espacio adicional al final para dispositivos más pequeños
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        
        // Botones de navegación en la parte superior (fuera del scroll y ajustados para la top bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 70.dp), // Aumentado para evitar la top bar
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón de acceso admin (izquierda)
            FloatingActionButton(
                onClick = { onNavigateToLogin(WelcomeUserType.ADMIN) },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = if (isLight) AdminColor else PrimaryLight,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Admin Login",
                    tint = if (isLight) AdminColor else PrimaryLight
                )
            }
            
            // Botón para cerrar la aplicación (derecha)
            FloatingActionButton(
                onClick = onCloseApp,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = Error,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close App",
                    tint = Error
                )
            }
        }
    }
}

@Composable
fun OnboardingCarousel() {
    val isLight = isLightTheme()

    val items = listOf(
        CarouselItem(
            icon = Icons.Default.School,
            title = "Comunicación Escuela-Familia",
            description = "Información diaria sobre la actividad de tu hijo/a en el centro educativo con notificaciones en tiempo real.",
            infoDetail = "Con UmeEgunero, mantén una conexión directa con los educadores de tus hijos."
        ),
        CarouselItem(
            icon = Icons.AutoMirrored.Filled.Chat,
            title = "Chat Privado y Seguro",
            description = "Comunicación directa y cifrada con profesores para resolver cualquier duda al instante.",
            infoDetail = "Mensajería con cifrado de extremo a extremo para garantizar la privacidad."
        ),
        CarouselItem(
            icon = Icons.Default.Build,
            title = "Informes Diarios Completos",
            description = "Control detallado de comidas, descanso, actividades y desarrollo educativo actualizado.",
            infoDetail = "Visualiza estadísticas y progreso en tiempo real con gráficos interactivos."
        ),
        CarouselItem(
            icon = Icons.Default.CalendarMonth,
            title = "Calendario Escolar",
            description = "Accede al calendario de eventos, excursiones y actividades del centro educativo.",
            infoDetail = "Sincroniza con tu calendario personal y recibe recordatorios automáticos."
        ),
        CarouselItem(
            icon = Icons.Default.Notifications,
            title = "Notificaciones Importantes",
            description = "Recibe alertas sobre cambios de horario, eventos especiales o necesidades específicas.",
            infoDetail = "Configura el nivel de prioridad para cada tipo de notificación según tus preferencias."
        )
    )

    var currentPage by remember { mutableIntStateOf(0) }

    // Control para habilitar/deshabilitar la animación automática
    var autoScrollEnabled by remember { mutableStateOf(true) }
    
    // Animación de transición suave entre slides
    val transition = updateTransition(targetState = currentPage, label = "Page Transition")
    
    // Animación de escala para el icono
    val iconScale by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "Icon Scale Animation"
    ) { page ->
        if (page == currentPage) 1f else 0.8f
    }
    
    // Animación de opacidad para el texto
    val textAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 500)
        },
        label = "Text Alpha Animation"
    ) { page ->
        if (page == currentPage) 1f else 0f
    }

    LaunchedEffect(key1 = autoScrollEnabled) {
        if (autoScrollEnabled) {
        while (true) {
            delay(5000)
            currentPage = (currentPage + 1) % items.size
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = if (isLight) 6.dp else 10.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (isLight)
                    Color.White.copy(alpha = 0.95f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (isLight) 
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                    Color.White
                                )
                            else 
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.surface
                                ),
                            radius = 800f
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                CarouselItemContent(
                    item = items[currentPage],
                    iconScale = iconScale,
                    textAlpha = textAlpha
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Controles de navegación y pausa
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón para ir atrás
            IconButton(
                onClick = { 
                    currentPage = if (currentPage > 0) currentPage - 1 else items.size - 1
                    // Al navegar manualmente, pausamos la rotación automática
                    autoScrollEnabled = false
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Indicadores y botón de pausa
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
        ) {
            repeat(items.size) { index ->
                val width by animateDpAsState(
                    targetValue = if (currentPage == index) 24.dp else 8.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "indicator width"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .clip(CircleShape)
                        .background(
                                if (currentPage == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                        .width(width)
                        .height(8.dp)
                            .clickable { 
                                currentPage = index
                                // Al navegar manualmente, pausamos la rotación automática
                                autoScrollEnabled = false
                            }
                    )
                }
                
                // Botón para pausar/reanudar la rotación automática
                IconButton(
                    onClick = { autoScrollEnabled = !autoScrollEnabled },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                ) {
                    Icon(
                        imageVector = if (autoScrollEnabled) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (autoScrollEnabled) "Pausar" else "Reanudar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Botón para ir adelante
            IconButton(
                onClick = { 
                    currentPage = (currentPage + 1) % items.size
                    // Al navegar manualmente, pausamos la rotación automática
                    autoScrollEnabled = false
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Siguiente pantalla",
                    tint = Color.White
                )
            }
        }
    }
}

data class CarouselItem(
    val icon: Any,
    val title: String,
    val description: String,
    val infoDetail: String = ""
)

@Composable
fun CarouselItemContent(
    item: CarouselItem,
    iconScale: Float = 1f,
    textAlpha: Float = 1f
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
    ) {
        when (item.icon) {
            is androidx.compose.ui.graphics.vector.ImageVector -> {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                        modifier = Modifier
                            .size(64.dp * iconScale),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            is Painter -> {
                Icon(
                    painter = item.icon,
                    contentDescription = item.title,
                        modifier = Modifier
                            .size(64.dp * iconScale),
                    tint = MaterialTheme.colorScheme.primary
                )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.graphicsLayer(alpha = textAlpha)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .graphicsLayer(alpha = textAlpha),
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (item.infoDetail.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = item.infoDetail,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .graphicsLayer(alpha = textAlpha * 0.8f),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun LoginButtons(
    onCentroLogin: () -> Unit,
    onProfesorLogin: () -> Unit,
    onFamiliarLogin: () -> Unit
) {
    val isLight = isLightTheme()
    var showBiometricInfo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón de acceso centro con diseño mejorado
        Button(
            onClick = onCentroLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLight) Color(0xFF0277BD) else MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 0.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Acceso Centro",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Acceso biométrico",
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Información biométrica",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { showBiometricInfo = true },
                tint = Color.White.copy(alpha = 0.8f)
            )
        }

        // Botón de acceso profesor con diseño mejorado
        Button(
            onClick = onProfesorLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLight) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 0.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Acceso Profesor",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Acceso biométrico",
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Información biométrica",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { showBiometricInfo = true },
                tint = Color.White.copy(alpha = 0.8f)
            )
        }

        // Botón de acceso familiar con diseño mejorado
        Button(
            onClick = onFamiliarLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLight) Color(0xFF512DA8) else MaterialTheme.colorScheme.tertiary
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 0.dp
            )
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Acceso Familiar",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Acceso biométrico",
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Información biométrica",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { showBiometricInfo = true },
                tint = Color.White.copy(alpha = 0.8f)
            )
        }
    }

    if (showBiometricInfo) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBiometricInfo = false },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Acceso Biométrico",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                Text(
                        "Para utilizar el acceso biométrico:",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                    
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "1",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Inicia sesión normalmente primero")
                    }
                    
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "2",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ve a Configuración > Seguridad")
                    }
                    
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "3",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Activa la autenticación biométrica")
                    }
                    
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "4",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("La próxima vez podrás acceder usando tu huella dactilar")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBiometricInfo = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Entendido")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBiometricInfo = false }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenLightPreview() {
    UmeEguneroTheme(darkTheme = false) {
        WelcomeScreen(
            onNavigateToLogin = {},
            onNavigateToRegister = {},
            onCloseApp = {},
            onDemoRequested = {},
            onNavigateToSupport = {},
            onNavigateToTechnicalSupport = {},
            onNavigateToFAQ = {}
        )
    }
}

@Composable
fun isLightTheme(): Boolean {
    return MaterialTheme.colorScheme.isLight()
}

// Componente de selección de idioma integrado directamente en el LazyColumn
@Composable
fun LanguageSelector(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(
        "Español" to "ES",
        "English" to "EN",
        "Euskara" to "EU",
        "Català" to "CA",
        "Galego" to "GL"
    )
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        languages.forEach { (name, code) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (currentLanguage == code)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else
                            Color.Transparent
                    )
                    .clickable { onLanguageSelected(code) }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (currentLanguage == code)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                
                if (currentLanguage == code) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// Diálogo de selección de idioma
@Composable
private fun LanguageSelectionDialog(
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        "Español" to "ES",
        "English" to "EN",
        "Euskara" to "EU",
        "Català" to "CA",
        "Galego" to "GL"
    )
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar idioma") },
        text = {
            Column {
                languages.forEach { (name, code) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { 
                                onLanguageSelected(code)
                                onDismiss()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = code,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun SupportButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}