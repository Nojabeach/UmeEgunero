package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.feature.common.academico.viewmodel.CalendarioViewModel
import com.tfg.umeegunero.feature.common.academico.viewmodel.CalendarioUiState
import com.tfg.umeegunero.ui.components.LoadingIndicator
import com.tfg.umeegunero.ui.components.calendar.SelectorVistaCalendario
import com.tfg.umeegunero.ui.components.calendar.TipoVistaCalendario
import com.tfg.umeegunero.ui.components.calendar.VistaCalendarioDiario
import com.tfg.umeegunero.ui.components.calendar.VistaCalendarioSemanal
import com.tfg.umeegunero.ui.components.ErrorScreen
import com.tfg.umeegunero.util.toLocalDate
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import android.content.res.Configuration
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.navigation.NavItem
import com.tfg.umeegunero.navigation.AppScreens

/**
 * Pantalla de gestión del calendario escolar
 * 
 * Esta pantalla permite visualizar y gestionar los eventos del calendario escolar
 * con diferentes vistas (mensual, semanal, diaria) y añadir nuevos eventos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    navController: NavController,
    viewModel: CalendarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var tipoVistaActual by remember { mutableStateOf(TipoVistaCalendario.MENSUAL) }
    
    // Efecto para cargar los eventos al inicio
    LaunchedEffect(Unit) {
        viewModel.loadEventos()
    }
    
    // Mostrar error en Snackbar si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.clearError()
        }
    }
    
    // Mostrar mensaje de éxito en Snackbar si existe
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            uiState.successMessage?.let {
                snackbarHostState.showSnackbar(message = it)
                viewModel.clearSuccess()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showEventDialog() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir evento"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mostrar indicador de carga cuando sea necesario
            if (uiState.isLoading) {
                LoadingIndicator()
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Selector del tipo de vista (mensual, semanal, diaria)
                    SelectorVistaCalendario(
                        vistaActual = tipoVistaActual,
                        onCambioVista = { tipoVistaActual = it }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Contenido según el tipo de vista seleccionado
                    when (tipoVistaActual) {
                        TipoVistaCalendario.MENSUAL -> {
                            VistaCalendarioMensual(
                                uiState = uiState,
                                onDateSelected = { viewModel.updateSelectedDate(it) },
                                onPrevMonth = {
                                    val prevMonth = uiState.currentMonth.minusMonths(1)
                                    viewModel.updateCurrentMonth(prevMonth)
                                },
                                onNextMonth = {
                                    val nextMonth = uiState.currentMonth.plusMonths(1)
                                    viewModel.updateCurrentMonth(nextMonth)
                                },
                                navController = navController
                            )
                        }
                        TipoVistaCalendario.SEMANAL -> {
                            VistaCalendarioSemanal(
                                fechaActual = uiState.selectedDate,
                                eventos = uiState.eventos,
                                onFechaSeleccionada = { 
                                    viewModel.updateSelectedDate(it)
                                    if (it.month != uiState.currentMonth.month) {
                                        viewModel.updateCurrentMonth(YearMonth.from(it))
                                    }
                                },
                                onSemanaAnterior = { viewModel.updateSelectedDate(uiState.selectedDate.minusWeeks(1)) },
                                onSemanaSiguiente = { viewModel.updateSelectedDate(uiState.selectedDate.plusWeeks(1)) },
                                onClickEvento = { evento -> 
                                    navController.navigate(AppScreens.DetalleEvento.createRoute(evento.id))
                                }
                            )
                        }
                        TipoVistaCalendario.DIARIA -> {
                            VistaCalendarioDiario(
                                fechaActual = uiState.selectedDate,
                                eventos = uiState.eventos,
                                onCambioFecha = { 
                                    viewModel.updateSelectedDate(it)
                                    if (it.month != uiState.currentMonth.month) {
                                        viewModel.updateCurrentMonth(YearMonth.from(it))
                                    }
                                },
                                onClickEvento = { evento -> 
                                    navController.navigate(AppScreens.DetalleEvento.createRoute(evento.id))
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Diálogo para añadir un nuevo evento
        if (uiState.showEventDialog) {
            Dialog(onDismissRequest = { viewModel.hideEventDialog() }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Encabezado con fecha seleccionada
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = "Nuevo evento",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    text = uiState.selectedDate.format(
                                        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
                                    ).replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Selector de tipo de evento con íconos
                        Text(
                            text = "Tipo de evento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Chips horizontales con scroll
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(TipoEvento.values()) { tipo ->
                                val isSelected = uiState.selectedEventType == tipo
                                val iconTint = if (isSelected) Color.White else tipo.color
                                val backgroundColor = if (isSelected) tipo.color else tipo.color.copy(alpha = 0.1f)
                                
                                Surface(
                                    onClick = { viewModel.updateSelectedEventType(tipo) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = backgroundColor,
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val icon = when(tipo) {
                                            TipoEvento.EXAMEN -> Icons.Default.School
                                            TipoEvento.ESCOLAR -> Icons.AutoMirrored.Filled.MenuBook
                                            TipoEvento.REUNION -> Icons.Default.Groups
                                            TipoEvento.FESTIVO -> Icons.Default.Celebration
                                            else -> Icons.Default.Event
                                        }
                                        
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = iconTint,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            text = tipo.name.replaceFirstChar { it.uppercase() },
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Campo para título del evento
                        OutlinedTextField(
                            value = uiState.eventTitle ?: "",
                            onValueChange = { viewModel.updateEventTitle(it) },
                            label = { Text("Título del evento") },
                            leadingIcon = { 
                                Icon(
                                    imageVector = Icons.Default.Title,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Campo para descripción del evento
                        OutlinedTextField(
                            value = uiState.eventDescription,
                            onValueChange = { viewModel.updateEventDescription(it) },
                            label = { Text("Descripción") },
                            leadingIcon = { 
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Campo para ubicación
                        OutlinedTextField(
                            value = uiState.eventLocation ?: "",
                            onValueChange = { viewModel.updateEventLocation(it) },
                            label = { Text("Ubicación (opcional)") },
                            leadingIcon = { 
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Selector de hora
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { 
                                    // Aquí abriría un selector de hora que llame a updateEventTime
                                    // Por ahora, simplemente establecemos una hora fija para demostración
                                    viewModel.updateEventTime("12:00")
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hora",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Text(
                                    text = uiState.eventTime ?: "Sin especificar",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Botones de acción
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.hideEventDialog() },
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Text("Cancelar")
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Button(
                                onClick = { viewModel.saveEvento() },
                                enabled = uiState.selectedEventType != null && 
                                         (uiState.eventTitle?.isNotBlank() == true || 
                                          uiState.eventDescription.isNotBlank()),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = uiState.selectedEventType?.color ?: MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text("Guardar evento")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vista mensual del calendario
 */
@Composable
fun VistaCalendarioMensual(
    uiState: CalendarioUiState,
    onDateSelected: (LocalDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    navController: NavController
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Sección superior con controles de navegación del mes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Mes anterior"
                )
            }
            
            Text(
                text = uiState.currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))),
                style = MaterialTheme.typography.titleLarge
            )
            
            IconButton(onClick = onNextMonth) {
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
            val firstOfMonth = uiState.currentMonth.atDay(1)
            val dayOfWeek = firstOfMonth.dayOfWeek.value % 7 // Domingo como primer día (0)
            
            val daysFromPreviousMonth = if (dayOfWeek > 0) dayOfWeek else 0
            val previousMonth = uiState.currentMonth.minusMonths(1)
            
            val daysInPreviousMonth = previousMonth.lengthOfMonth()
            val daysInCurrentMonth = uiState.currentMonth.lengthOfMonth()
            
            val previousMonthDays = (daysInPreviousMonth - daysFromPreviousMonth + 1..daysInPreviousMonth).map { day ->
                CalendarDay(previousMonth.atDay(day), isCurrentMonth = false)
            }
            
            val currentMonthDays = (1..daysInCurrentMonth).map { day ->
                CalendarDay(uiState.currentMonth.atDay(day), isCurrentMonth = true)
            }
            
            val nextMonth = uiState.currentMonth.plusMonths(1)
            val remainingDays = 42 - (previousMonthDays.size + currentMonthDays.size)
            
            val nextMonthDays = (1..remainingDays).map { day ->
                CalendarDay(nextMonth.atDay(day), isCurrentMonth = false)
            }
            
            val allDays = previousMonthDays + currentMonthDays + nextMonthDays
            
            items(allDays) { day ->
                // Verificar si hay eventos para este día
                val dayEvents = uiState.eventos.filter { evento ->
                    evento.fecha.toLocalDate() == day.date
                }
                
                val isSelected = day.date == uiState.selectedDate
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
                            onDateSelected(day.date)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (!day.isCurrentMonth) Color.Gray else Color.Unspecified
                            )
                        )
                        
                        // Indicador de eventos
                        if (dayEvents.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Lista de eventos del día seleccionado
        Text(
            text = "Eventos del ${uiState.selectedDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES")))}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val selectedDateEvents = uiState.eventos.filter { 
            it.fecha.toLocalDate() == uiState.selectedDate 
        }
        
        if (selectedDateEvents.isEmpty()) {
            Text(
                text = "No hay eventos para este día",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(selectedDateEvents) { evento ->
                    EventoItem(
                        evento = evento,
                        onClick = { 
                            navController.navigate(AppScreens.DetalleEvento.createRoute(evento.id))
                        }
                    )
                }
            }
        }
    }
}

/**
 * Un día en el calendario
 */
data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)

/**
 * Item para mostrar un evento en la lista
 */
@Composable
fun EventoItem(
    evento: Evento,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador del tipo de evento
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = when (evento.tipo) {
                            TipoEvento.EXAMEN -> Color(0xFFE57373) // Rojo claro
                            TipoEvento.ESCOLAR -> Color(0xFF81C784) // Verde claro
                            TipoEvento.REUNION -> Color(0xFF64B5F6) // Azul claro
                            TipoEvento.FESTIVO -> Color(0xFFFFD54F) // Amarillo
                            else -> Color.Gray
                        },
                        shape = MaterialTheme.shapes.small
                    )
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evento.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = evento.tipo.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalles"
            )
        }
    }
}

/**
 * Vista previa de la pantalla de calendario
 */
@Preview(showBackground = true)
@Composable
fun CalendarioScreenPreview() {
    UmeEguneroTheme {
        CalendarioScreen(
            navController = rememberNavController()
        )
    }
}

/**
 * Vista previa de la pantalla de calendario en modo oscuro
 */
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CalendarioScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        CalendarioScreen(
            navController = rememberNavController()
        )
    }
} 