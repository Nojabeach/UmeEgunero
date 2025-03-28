package com.tfg.umeegunero.feature.familiar.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioFamiliaScreen(
    navController: NavController
) {
    // Estado para almacenar la fecha seleccionada
    var fechaSeleccionada by remember { mutableStateOf(Calendar.getInstance()) }
    
    // Datos de ejemplo para eventos
    val eventos = remember {
        listOf(
            Evento(
                id = "1",
                titulo = "Reunión de padres",
                descripcion = "Reunión de padres y profesores para discutir el progreso de los alumnos",
                fecha = Calendar.getInstance().apply { 
                    set(Calendar.HOUR_OF_DAY, 17)
                    set(Calendar.MINUTE, 0)
                    add(Calendar.DAY_OF_MONTH, 2)
                }.time,
                ubicacion = "Salón de actos",
                tipo = TipoEvento.REUNION,
                alumnoId = null
            ),
            Evento(
                id = "2",
                titulo = "Excursión al museo",
                descripcion = "Visita al museo de ciencias naturales",
                fecha = Calendar.getInstance().apply { 
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                    add(Calendar.DAY_OF_MONTH, 5)
                }.time,
                ubicacion = "Museo de Ciencias Naturales",
                tipo = TipoEvento.EXCURSION,
                alumnoId = "1"
            ),
            Evento(
                id = "3",
                titulo = "Entrega de proyectos",
                descripcion = "Fecha límite para la entrega de proyectos de ciencias",
                fecha = Calendar.getInstance().apply { 
                    set(Calendar.HOUR_OF_DAY, 14)
                    set(Calendar.MINUTE, 0)
                    add(Calendar.DAY_OF_MONTH, 7)
                }.time,
                ubicacion = "Aula de ciencias",
                tipo = TipoEvento.TAREA,
                alumnoId = "1"
            ),
            Evento(
                id = "4",
                titulo = "Festival de primavera",
                descripcion = "Celebración del festival de primavera con actividades para toda la familia",
                fecha = Calendar.getInstance().apply { 
                    set(Calendar.HOUR_OF_DAY, 16)
                    set(Calendar.MINUTE, 30)
                    add(Calendar.DAY_OF_MONTH, 10)
                }.time,
                ubicacion = "Patio del colegio",
                tipo = TipoEvento.EVENTO_ESPECIAL,
                alumnoId = null
            )
        )
    }
    
    // Filtrar eventos para la fecha seleccionada
    val eventosFiltrados = eventos.filter { evento ->
        val eventoCalendar = Calendar.getInstance().apply { time = evento.fecha }
        eventoCalendar.get(Calendar.YEAR) == fechaSeleccionada.get(Calendar.YEAR) &&
        eventoCalendar.get(Calendar.MONTH) == fechaSeleccionada.get(Calendar.MONTH) &&
        eventoCalendar.get(Calendar.DAY_OF_MONTH) == fechaSeleccionada.get(Calendar.DAY_OF_MONTH)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario Escolar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { /* Filtrar eventos */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = MaterialTheme.colorScheme.onPrimary
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
                    fechaSeleccionada = fechaSeleccionada.clone() as Calendar
                    fechaSeleccionada.add(Calendar.MONTH, -1)
                },
                onNextMonth = {
                    fechaSeleccionada = fechaSeleccionada.clone() as Calendar
                    fechaSeleccionada.add(Calendar.MONTH, 1)
                }
            )
            
            CalendarioGrid(
                fechaSeleccionada = fechaSeleccionada,
                onDaySelected = { dia ->
                    fechaSeleccionada = fechaSeleccionada.clone() as Calendar
                    fechaSeleccionada.set(Calendar.DAY_OF_MONTH, dia)
                },
                eventos = eventos
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de eventos para el día seleccionado
            Text(
                text = "Eventos del día",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (eventosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay eventos para este día",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(eventosFiltrados) { evento ->
                        EventoCard(
                            evento = evento,
                            onClick = { /* Ver detalle del evento */ }
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
        Text(
            text = dateFormat.format(fechaSeleccionada.time).capitalize(),
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
    eventos: List<Evento>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Días de la semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")
            diasSemana.forEach { dia ->
                Text(
                    text = dia,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Días del mes
        val cal = fechaSeleccionada.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val primerDia = cal.get(Calendar.DAY_OF_WEEK)
        val diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val filas = (diasEnMes + primerDia - 2) / 7 + 1
        
        // Ajustar primerDia para que lunes sea 0 (en Calendar, domingo es 1)
        val offsetPrimerDia = if (primerDia == Calendar.SUNDAY) 6 else primerDia - 2
        
        for (i in 0 until filas) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (j in 0 until 7) {
                    val diaIndex = i * 7 + j - offsetPrimerDia
                    
                    if (diaIndex >= 0 && diaIndex < diasEnMes) {
                        val dia = diaIndex + 1
                        val esDiaActual = fechaSeleccionada.get(Calendar.DAY_OF_MONTH) == dia
                        
                        // Verificar si hay eventos para este día
                        val hayEventos = eventos.any { evento ->
                            val eventoCalendar = Calendar.getInstance().apply { time = evento.fecha }
                            eventoCalendar.get(Calendar.YEAR) == fechaSeleccionada.get(Calendar.YEAR) &&
                            eventoCalendar.get(Calendar.MONTH) == fechaSeleccionada.get(Calendar.MONTH) &&
                            eventoCalendar.get(Calendar.DAY_OF_MONTH) == dia
                        }
                        
                        Box(modifier = Modifier.weight(1f)) {
                            CalendarioDia(
                                dia = dia,
                                esDiaActual = esDiaActual,
                                hayEventos = hayEventos,
                                onClick = { onDaySelected(dia) }
                            )
                        }
                    } else {
                        // Día vacío
                        Box(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CalendarioDia(
    dia: Int,
    esDiaActual: Boolean,
    hayEventos: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                if (esDiaActual) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .padding(4.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dia.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (esDiaActual) FontWeight.Bold else FontWeight.Normal,
                color = if (esDiaActual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
            )
            
            if (hayEventos) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun EventoCard(
    evento: Evento,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
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
                Text(
                    text = timeFormat.format(evento.fecha),
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
    val fecha: Date,
    val ubicacion: String,
    val tipo: TipoEvento,
    val alumnoId: String? // null si es un evento general
)

enum class TipoEvento(val color: Color, val icon: ImageVector) {
    REUNION(Color(0xFF1976D2), Icons.Default.People),
    EXCURSION(Color(0xFF388E3C), Icons.Default.Place),
    TAREA(Color(0xFFF57C00), Icons.Default.Assignment),
    EVENTO_ESPECIAL(Color(0xFF7B1FA2), Icons.Default.Star)
}

// Extensión para capitalizar la primera letra
private fun String.capitalize(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1)
    } else {
        this
    }
} 