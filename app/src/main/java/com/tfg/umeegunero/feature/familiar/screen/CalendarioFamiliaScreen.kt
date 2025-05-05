package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*
import timber.log.Timber
import com.tfg.umeegunero.ui.theme.FamiliarColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioFamiliaScreen(
    navController: NavController
) {
    // Estado para la fecha seleccionada
    var fechaSeleccionada by remember { mutableStateOf(Calendar.getInstance()) }
    val haptic = LocalHapticFeedback.current
    
    // Lista de eventos de ejemplo
    val eventos = remember {
        listOf(
            Evento(
                id = "1",
                titulo = "Reunión con padres",
                descripcion = "Reunión trimestral para hablar sobre el progreso de los niños",
                fecha = Calendar.getInstance().timeInMillis,
                ubicacion = "Sala de reuniones",
                tipo = TipoEvento.REUNION
            ),
            Evento(
                id = "2",
                titulo = "Excursión al zoológico",
                descripcion = "Visita educativa al zoológico de la ciudad",
                fecha = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 2) }.timeInMillis,
                ubicacion = "Zoológico de la ciudad",
                tipo = TipoEvento.EXCURSION
            ),
            Evento(
                id = "3",
                titulo = "Fiesta de primavera",
                descripcion = "Celebración de la llegada de la primavera con actividades especiales",
                fecha = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 5) }.timeInMillis,
                ubicacion = "Patio principal",
                tipo = TipoEvento.FIESTA
            ),
            Evento(
                id = "4",
                titulo = "Vacunación",
                descripcion = "Jornada de vacunación para todos los niños",
                fecha = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 10) }.timeInMillis,
                ubicacion = "Enfermería",
                tipo = TipoEvento.SALUD
            ),
            Evento(
                id = "5",
                titulo = "Festivo local",
                descripcion = "Día festivo por fiestas patronales",
                fecha = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 15) }.timeInMillis,
                ubicacion = "",
                tipo = TipoEvento.FESTIVO
            )
        )
    }
    
    // Filtrado de eventos para el día seleccionado
    val eventosDiaSeleccionado = eventos.filter { evento ->
        val fechaEvento = Calendar.getInstance().apply { timeInMillis = evento.fecha }
        fechaEvento.get(Calendar.YEAR) == fechaSeleccionada.get(Calendar.YEAR) &&
        fechaEvento.get(Calendar.MONTH) == fechaSeleccionada.get(Calendar.MONTH) &&
        fechaEvento.get(Calendar.DAY_OF_MONTH) == fechaSeleccionada.get(Calendar.DAY_OF_MONTH)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario Escolar") },
                navigationIcon = {
                    IconButton(onClick = { 
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
                        navController.popBackStack() 
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FamiliarColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { 
                        try {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } catch (e: Exception) {
                            Timber.e(e, "Error al realizar feedback háptico")
                        }
                        /* Filtrar eventos */ 
                    }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Crear nuevo evento */ },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir evento",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Calendario simplificado
            CalendarioHeader(
                fechaSeleccionada = fechaSeleccionada,
                onPrevMonth = {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (e: Exception) {
                        Timber.e(e, "Error al realizar feedback háptico")
                    }
                    fechaSeleccionada = fechaSeleccionada.clone() as Calendar
                    fechaSeleccionada.add(Calendar.MONTH, -1)
                },
                onNextMonth = {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (e: Exception) {
                        Timber.e(e, "Error al realizar feedback háptico")
                    }
                    fechaSeleccionada = fechaSeleccionada.clone() as Calendar
                    fechaSeleccionada.add(Calendar.MONTH, 1)
                }
            )
            
            CalendarioGrid(
                fechaSeleccionada = fechaSeleccionada,
                onDaySelected = { dia ->
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } catch (e: Exception) {
                        Timber.e(e, "Error al realizar feedback háptico")
                    }
                    fechaSeleccionada = fechaSeleccionada.clone() as Calendar
                    fechaSeleccionada.set(Calendar.DAY_OF_MONTH, dia)
                },
                eventos = eventos,
                haptic = haptic
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de eventos para el día seleccionado
            Text(
                text = "Eventos del día",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (eventosDiaSeleccionado.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay eventos para este día",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn {
                    items(eventosDiaSeleccionado) { evento ->
                        EventoItem(
                            evento = evento,
                            onClick = {
                                try {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                } catch (e: Exception) {
                                    Timber.e(e, "Error al realizar feedback háptico")
                                }
                                // Navegar a detalle de evento
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarioHeader(
    fechaSeleccionada: Calendar,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevMonth) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Mes anterior"
            )
        }
        
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        val mesFormateado = dateFormat.format(fechaSeleccionada.time)
        val mesConMayuscula = mesFormateado.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        Text(
            text = mesConMayuscula,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Mes siguiente"
            )
        }
    }
}

@Composable
fun CalendarioGrid(
    fechaSeleccionada: Calendar,
    onDaySelected: (Int) -> Unit,
    eventos: List<Evento>,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    // Lógica para determinar los días del mes
    val diasMes = remember(fechaSeleccionada.get(Calendar.YEAR), fechaSeleccionada.get(Calendar.MONTH)) {
        val calendar = fechaSeleccionada.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        val dias = mutableListOf<Int>()
        
        // Añadir días en blanco al inicio para alinear con el primer día de la semana
        for (i in 1 until firstDayOfWeek) {
            dias.add(0) // 0 representa un día vacío
        }
        
        // Añadir los días del mes
        for (i in 1..maxDays) {
            dias.add(i)
        }
        
        dias
    }
    
    // Determinar los días con eventos
    val diasConEventos = remember(fechaSeleccionada.get(Calendar.YEAR), fechaSeleccionada.get(Calendar.MONTH), eventos) {
        eventos.mapNotNull { evento ->
            val eventoCalendar = Calendar.getInstance().apply { timeInMillis = evento.fecha }
            if (eventoCalendar.get(Calendar.YEAR) == fechaSeleccionada.get(Calendar.YEAR) &&
                eventoCalendar.get(Calendar.MONTH) == fechaSeleccionada.get(Calendar.MONTH)) {
                eventoCalendar.get(Calendar.DAY_OF_MONTH)
            } else null
        }.toSet()
    }
    
    // Cabecera con nombres de días
        Row(
            modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
        ) {
        listOf("D", "L", "M", "X", "J", "V", "S").forEach { dia ->
                Text(
                    text = dia,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
    // Cuadrícula de días
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        val rows = diasMes.chunked(7)
        rows.forEach { semana ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                semana.forEach { dia ->
                    val hoy = Calendar.getInstance()
                    val esHoy = dia != 0 && 
                               hoy.get(Calendar.YEAR) == fechaSeleccionada.get(Calendar.YEAR) &&
                               hoy.get(Calendar.MONTH) == fechaSeleccionada.get(Calendar.MONTH) &&
                               hoy.get(Calendar.DAY_OF_MONTH) == dia
                    
                    val esDiaSeleccionado = dia != 0 && 
                                           fechaSeleccionada.get(Calendar.DAY_OF_MONTH) == dia
                    
                    val tieneEventos = dia != 0 && diasConEventos.contains(dia)
                        
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dia != 0) {
                            DiaItem(
                                dia = dia,
                                esHoy = esHoy,
                                esDiaSeleccionado = esDiaSeleccionado,
                                tieneEventos = tieneEventos,
                                onClick = { 
                                    try {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } catch (e: Exception) {
                                        Timber.e(e, "Error al realizar feedback háptico")
                                    }
                                    onDaySelected(dia) 
                        }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DiaItem(
    dia: Int,
    esHoy: Boolean,
    esDiaSeleccionado: Boolean,
    tieneEventos: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                when {
                    esDiaSeleccionado -> FamiliarColor
                    esHoy -> FamiliarColor.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .clickable(
                enabled = true,
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dia.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    esDiaSeleccionado -> Color.White
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (tieneEventos) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (esDiaSeleccionado) Color.White
                            else FamiliarColor
                        )
                )
            }
        }
    }
}

@Composable
fun EventoItem(
    evento: Evento,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de tipo de evento
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(evento.tipo.color)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del evento
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = Date(evento.fecha)
                Text(
                    text = timeFormat.format(date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (evento.ubicacion.isNotEmpty()) {
                    Text(
                        text = evento.ubicacion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Icono según tipo de evento
            Icon(
                imageVector = evento.tipo.icon,
                contentDescription = null,
                tint = evento.tipo.color
            )
        }
    }
}

// Modelos de datos para el calendario
data class Evento(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val fecha: Long,
    val ubicacion: String,
    val tipo: TipoEvento
)

enum class TipoEvento(val color: Color, val icon: ImageVector) {
    REUNION(Color(0xFF1976D2), Icons.Default.Groups),
    EXCURSION(Color(0xFF43A047), Icons.Default.DirectionsBus),
    FIESTA(Color(0xFFE53935), Icons.Default.Celebration),
    SALUD(Color(0xFF8E24AA), Icons.Default.HealthAndSafety),
    FESTIVO(Color(0xFFFF9800), Icons.Default.Event),
    OTRO(Color(0xFF757575), Icons.Default.Info)
}

// Extensión para capitalizar la primera letra
private fun String.capitalize(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1)
    } else {
        this
    }
} 