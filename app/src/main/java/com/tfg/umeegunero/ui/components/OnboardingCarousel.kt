package com.tfg.umeegunero.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.delay

/**
 * Componente que muestra un carrusel de elementos de onboarding.
 * 
 * @param items Lista de elementos a mostrar en el carrusel
 * @param modifier Modificador opcional para personalizar el diseño
 */
@Composable
fun OnboardingCarousel(
    items: List<CarouselItem>,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(0) }
    var autoScrollEnabled by remember { mutableStateOf(true) }
    
    val transition = updateTransition(targetState = currentPage, label = "Page Transition")
    
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
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 6.dp
            ),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
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
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingCarouselPreview() {
    UmeEguneroTheme {
        Surface {
            OnboardingCarousel(
                items = listOf(
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
            )
        }
    }
} 