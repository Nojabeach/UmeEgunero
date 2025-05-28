package com.tfg.umeegunero.feature.common.welcome.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.R
import com.tfg.umeegunero.ui.theme.*
import com.tfg.umeegunero.ui.components.*
import com.tfg.umeegunero.util.ThemeUtils
import com.tfg.umeegunero.util.getUserColor
import com.tfg.umeegunero.feature.common.welcome.viewmodel.WelcomeViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin
import com.tfg.umeegunero.data.model.TipoUsuario
import com.tfg.umeegunero.ui.theme.CentroColor
import com.tfg.umeegunero.ui.theme.GradientStart
import com.tfg.umeegunero.ui.theme.GradientEnd
import com.tfg.umeegunero.ui.theme.AcademicoColor
import com.tfg.umeegunero.ui.theme.AcademicoColorDark
import timber.log.Timber

/**
 * Tipos de usuario disponibles para la selección en la pantalla de bienvenida.
 */
enum class WelcomeUserType {
    ADMIN,
    CENTRO,
    PROFESOR,
    FAMILIAR
}

@Composable
fun WelcomeScreen(
    onNavigateToLogin: (WelcomeUserType) -> Unit,
    onNavigateToRegister: () -> Unit = {},
    onCloseApp: () -> Unit,
    onNavigateToTechnicalSupport: () -> Unit = {},
    onNavigateToFAQ: () -> Unit = {},
    onNavigateToTerminosCondiciones: () -> Unit = {},
    viewModel: WelcomeViewModel = hiltViewModel()
) {
    val isLight = ThemeUtils.isLightTheme()
    
    // Iniciar sincronización cuando la pantalla esté visible
    LaunchedEffect(Unit) {
        Timber.d("WelcomeScreen visible, iniciando sincronización si es necesario")
        viewModel.iniciarSincronizacionSiEsNecesario()
    }
    
    // Definición de tonos pastel a partir de los colores principales
    val CentroColorPastel = CentroColor.copy(alpha = 0.25f)
    val ProfesorColorPastel = ProfesorColor.copy(alpha = 0.22f)
    val FamiliarColorPastel = FamiliarColor.copy(alpha = 0.22f)

    // Colores de fondo pastel para el gradiente principal
    val gradientColors = listOf(
        CentroColorPastel,
        ProfesorColorPastel,
        FamiliarColorPastel
    )
    
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
        val primaryColorTransparent = CentroColor.copy(alpha = 0.18f)
        val secondaryColorTransparent = AcademicoColor.copy(alpha = 0.18f)
        val tertiaryColorTransparent = GradientEnd.copy(alpha = 0.13f)
        val primaryLineColor = CentroColor.copy(alpha = 0.25f)
        val secondaryLineColor = AcademicoColor.copy(alpha = 0.18f)
        val tertiaryLineColor = GradientEnd.copy(alpha = 0.15f)
        
        // Colores y valores para las ondas en tonos pastel
        val waveColors = listOf(
            CentroColorPastel,
            ProfesorColorPastel,
            FamiliarColorPastel
        )
        
        // Elementos decorativos de fondo mejorados - usando un Canvas composable
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Círculos dinámicos con mayor tamaño y visibilidad
            repeat(25) { i ->
                val xPos =
                    (canvasWidth * (i / 25f + offset * 0.6f)) % (canvasWidth * 1.2f) - 100f
                val yPos = (canvasHeight * ((i % 8) / 8f)) - 50f
                val radius =
                    (30f + (i % 5) * 25f) * (if (i % 3 == 0) pulseAnimation * 0.7f else 1f)
                
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
                        .padding(top = 32.dp, bottom = 4.dp)
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
                            containerColor = if (ThemeUtils.isLightTheme())
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
                                    modifier = Modifier.graphicsLayer(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            // Carrusel de onboarding
            item {
                val carouselItems = listOf(
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
                OnboardingCarousel(
                    items = carouselItems
                )
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
                            containerColor = if (ThemeUtils.isLightTheme())
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (ThemeUtils.isLightTheme()) 4.dp else 6.dp
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = if (ThemeUtils.isLightTheme()) {
                                            listOf(
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                MaterialTheme.colorScheme.surface
                                            )
                                        } else {
                                            listOf(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                                    alpha = 0.7f
                                                ),
                                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                                    alpha = 0.95f
                                                )
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

                    // Sección de registro familiar
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
                            containerColor = if (ThemeUtils.isLightTheme())
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (ThemeUtils.isLightTheme()) 4.dp else 6.dp
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(16.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = if (ThemeUtils.isLightTheme()) {
                                            listOf(
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                MaterialTheme.colorScheme.surface
                                            )
                                        } else {
                                            listOf(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                                    alpha = 0.7f
                                                ),
                                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                                    alpha = 0.95f
                                                )
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
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FamiliarColor
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Registrarse como familiar",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            // Botones de soporte y términos
            item {
                SupportAndTermsButtons(
                    onNavigateToTechnicalSupport = onNavigateToTechnicalSupport,
                    onNavigateToFAQ = onNavigateToFAQ,
                    onNavigateToTerminosCondiciones = onNavigateToTerminosCondiciones
                )
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
                contentColor = if (ThemeUtils.isLightTheme()) AdminColor else AppColors.PrimaryLight,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Admin Login",
                    tint = if (ThemeUtils.isLightTheme()) AdminColor else AppColors.PrimaryLight
                )
            }
            
            // Botón para cerrar la aplicación (derecha)
            FloatingActionButton(
                onClick = onCloseApp,
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = AppColors.Error,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close App",
                    tint = AppColors.Error
                )
            }
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
            onNavigateToTechnicalSupport = {},
                onNavigateToFAQ = {},
                onNavigateToTerminosCondiciones = {}
            )
        }
    }
}