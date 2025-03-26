package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.feature.common.academico.viewmodel.CalendarioViewModel
import com.tfg.umeegunero.ui.components.LoadingIndicator
import java.time.LocalDate
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
    navController: NavController,
    viewModel: CalendarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    
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
                    IconButton(onClick = { viewModel.showEventDialog() }) {
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
                onClick = { viewModel.showEventDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir evento")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(message = "Cargando eventos...")
            }
        } else {
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
                        onClick = { viewModel.updateCurrentMonth(uiState.currentMonth.minusMonths(1)) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Mes anterior"
                        )
                    }
                    
                    Text(
                        text = uiState.currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))),
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    IconButton(
                        onClick = { viewModel.updateCurrentMonth(uiState.currentMonth.plusMonths(1)) }
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
                                    viewModel.updateSelectedDate(day.date)
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
                            text = "Eventos para ${uiState.selectedDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale("es", "ES")))}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val eventosDelDia = uiState.eventos.filter { it.fecha.toLocalDate() == uiState.selectedDate }
                        
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
                                                    viewModel.deleteEvento(evento.id)
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
        }
        
        // Diálogo para añadir un nuevo evento
        if (uiState.showEventDialog) {
            Dialog(
                onDismissRequest = { viewModel.hideEventDialog() }
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
                            text = "Nuevo evento para ${uiState.selectedDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES")))}",
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
                                    selected = uiState.selectedEventType == tipo,
                                    onClick = { viewModel.updateSelectedEventType(tipo) },
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
                            value = uiState.eventDescription,
                            onValueChange = { viewModel.updateEventDescription(it) },
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
                                onClick = { viewModel.hideEventDialog() }
                            ) {
                                Text("Cancelar")
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = { viewModel.saveEvento() },
                                enabled = uiState.selectedEventType != null && uiState.eventDescription.isNotEmpty()
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