package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.navigation.AppScreens
import com.tfg.umeegunero.ui.components.LoadingIndicator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import android.content.res.Configuration
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import androidx.navigation.compose.rememberNavController

/**
 * Pantalla de gestión del calendario escolar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    navController: NavController
) {
    // Variables para controlar el estado del calendario
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showEventDialog by remember { mutableStateOf(false) }
    var selectedEventType by remember { mutableStateOf<TipoEvento?>(null) }
    var eventDescription by remember { mutableStateOf("") }
    
    // Lista de eventos (simulada)
    val events = remember { mutableStateListOf<EventoCalendario>() }
    
    // Calcular los días del mes actual
    val diasDelMes = remember(currentMonth) {
        val firstOfMonth = currentMonth.atDay(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value % 7 // Domingo como primer día (0)
        
        val daysFromPreviousMonth = if (dayOfWeek > 0) dayOfWeek else 0
        val previousMonth = currentMonth.minusMonths(1)
        
        val daysInPreviousMonth = previousMonth.lengthOfMonth()
        val daysInCurrentMonth = currentMonth.lengthOfMonth()
        
        val previousMonthDays = (daysInPreviousMonth - daysFromPreviousMonth + 1..daysInPreviousMonth).map { day ->
            CalendarDay(previousMonth.atDay(day), isCurrentMonth = false)
        }
        
        val currentMonthDays = (1..daysInCurrentMonth).map { day ->
            CalendarDay(currentMonth.atDay(day), isCurrentMonth = true)
        }
        
        val nextMonth = currentMonth.plusMonths(1)
        val remainingDays = 42 - (previousMonthDays.size + currentMonthDays.size)
        
        val nextMonthDays = (1..remainingDays).map { day ->
            CalendarDay(nextMonth.atDay(day), isCurrentMonth = false)
        }
        
        previousMonthDays + currentMonthDays + nextMonthDays
    }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Calendario Escolar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Botón para añadir evento
                    IconButton(onClick = { showEventDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir evento",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showEventDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir evento")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Sección superior con controles de navegación del mes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentMonth = currentMonth.minusMonths(1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Mes anterior"
                    )
                }
                
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))),
                    style = MaterialTheme.typography.titleLarge
                )
                
                IconButton(
                    onClick = { currentMonth = currentMonth.plusMonths(1) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Mes siguiente"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Cabecera con los días de la semana
            Row(modifier = Modifier.fillMaxWidth()) {
                val dayNames = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
                dayNames.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Cuadrícula del calendario
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                items(diasDelMes) { day ->
                    // Verificar si hay eventos para este día
                    val dayEvents = events.filter { evento ->
                        evento.fecha.toLocalDate() == day.date
                    }
                    
                    val isSelected = day.date == selectedDate
                    val isToday = day.date == LocalDate.now()
                    
                    // Día del calendario
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.secondary
                                    else -> Color.LightGray
                                }
                            )
                            .background(
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    day.isCurrentMonth -> Color.White
                                    else -> Color.LightGray.copy(alpha = 0.2f)
                                }
                            )
                            .clickable {
                                selectedDate = day.date
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            // Número del día
                            Text(
                                text = day.date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (day.isCurrentMonth) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }
                            )
                            
                            // Indicador de eventos
                            if (dayEvents.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = dayEvents.first().tipo.color,
                                            shape = MaterialTheme.shapes.small
                                        )
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sección inferior con eventos del día seleccionado
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Eventos para ${selectedDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "ES")))}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val eventosDelDia = events.filter { it.fecha.toLocalDate() == selectedDate }
                    
                    if (eventosDelDia.isEmpty()) {
                        Text(
                            text = "No hay eventos programados para este día",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(200.dp)
                        ) {
                            items(eventosDelDia) { evento ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = evento.tipo.color.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(
                                                    color = evento.tipo.color,
                                                    shape = MaterialTheme.shapes.small
                                                )
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = evento.tipo.nombre,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            
                                            Text(
                                                text = evento.descripcion,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        
                                        IconButton(
                                            onClick = {
                                                events.remove(evento)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar evento",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Diálogo para añadir un nuevo evento
        if (showEventDialog) {
            Dialog(
                onDismissRequest = { 
                    showEventDialog = false
                    selectedEventType = null
                    eventDescription = ""
                }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Nuevo evento para ${selectedDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES")))}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Selector de tipo de evento
                        Text(
                            text = "Tipo de evento",
                            style = MaterialTheme.typography.labelMedium
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TipoEvento.values().forEach { tipo ->
                                FilterChip(
                                    selected = selectedEventType == tipo,
                                    onClick = { selectedEventType = tipo },
                                    label = { Text(tipo.nombre) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(
                                                    color = tipo.color,
                                                    shape = MaterialTheme.shapes.small
                                                )
                                        )
                                    }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Campo para la descripción
                        TextField(
                            value = eventDescription,
                            onValueChange = { eventDescription = it },
                            label = { Text("Descripción del evento") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Botones de acción
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    showEventDialog = false
                                    selectedEventType = null
                                    eventDescription = ""
                                }
                            ) {
                                Text("Cancelar")
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = {
                                    if (selectedEventType != null && eventDescription.isNotEmpty()) {
                                        // Crear y añadir el nuevo evento
                                        val newEvent = EventoCalendario(
                                            id = UUID.randomUUID().toString(),
                                            fecha = selectedDate.atStartOfDay(),
                                            tipo = selectedEventType!!,
                                            descripcion = eventDescription
                                        )
                                        events.add(newEvent)
                                        
                                        // Resetear el formulario y cerrar el diálogo
                                        selectedEventType = null
                                        eventDescription = ""
                                        showEventDialog = false
                                    }
                                },
                                enabled = selectedEventType != null && eventDescription.isNotEmpty()
                            ) {
                                Text("Guardar")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Clase que representa un día en el calendario
 */
data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)

/**
 * Enumeración de tipos de eventos en el calendario
 */
enum class TipoEvento(val nombre: String, val color: Color) {
    FESTIVO("Festivo", Color.Red),
    ESCOLAR("Escolar", Color.Blue),
    EXCURSION("Excursión", Color.Green),
    REUNION("Reunión", Color(0xFF9C27B0))  // Púrpura
}

/**
 * Clase que representa un evento en el calendario
 */
data class EventoCalendario(
    val id: String,
    val fecha: java.time.LocalDateTime,
    val tipo: TipoEvento,
    val descripcion: String
)

@Preview(showBackground = true)
@Composable
fun CalendarioScreenPreview() {
    UmeEguneroTheme {
        CalendarioScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CalendarioScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        CalendarioScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EventoCalendarioItemPreview() {
    UmeEguneroTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = TipoEvento.REUNION.color.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = TipoEvento.REUNION.color,
                            shape = MaterialTheme.shapes.small
                        )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Reunión",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Evaluación del primer trimestre",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
} 