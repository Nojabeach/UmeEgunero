package com.tfg.umeegunero.feature.profesor.screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Assignment
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.feature.profesor.viewmodel.CalendarioViewModel
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import com.tfg.umeegunero.util.toLocalDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.geometry.Offset

/**
 * Pantalla de calendario para profesores
 * Permite visualizar y gestionar eventos escolares
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioProfesorScreen(
    navController: NavController,
    viewModel: CalendarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Mostrar error si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    // Mostrar mensaje de éxito
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Calendario Escolar") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfesorColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.mostrarDialogoCrearEvento() },
                containerColor = ProfesorColor,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir evento"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Indicador de carga
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Cabecera con selector de mes/año
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.mesAnterior() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Mes anterior",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Text(
                        text = uiState.mesSeleccionado.format(
                            DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
                        ).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = { viewModel.mesSiguiente() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Mes siguiente",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Días de la semana
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")
                    diasSemana.forEach { dia ->
                        Text(
                            text = dia,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Rejilla de días del mes
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Añadir días vacíos para alinear el primer día del mes
                    val primerDiaMes = uiState.mesSeleccionado.atDay(1).dayOfWeek.value
                    val diasVacios = if (primerDiaMes == 7) 0 else primerDiaMes
                    
                    items(diasVacios) {
                        Box(modifier = Modifier.size(40.dp))
                    }
                    
                    // Días del mes
                    val diasEnMes = uiState.mesSeleccionado.lengthOfMonth()
                    items(diasEnMes) { dia ->
                        val fecha = uiState.mesSeleccionado.atDay(dia + 1)
                        val esHoy = fecha.equals(LocalDate.now())
                        val esDiaSeleccionado = fecha.equals(uiState.diaSeleccionado)
                        val tieneEventos = uiState.eventos.any { 
                            it.fecha.toLocalDate().equals(fecha)
                        }
                        
                        DiaCalendario(
                            dia = dia + 1,
                            esHoy = esHoy,
                            esDiaSeleccionado = esDiaSeleccionado,
                            tieneEventos = tieneEventos,
                            onClick = { viewModel.seleccionarDia(fecha) }
                        )
                    }
                }
                
                AnimatedVisibility(
                    visible = uiState.mostrarEventos,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    // Lista de eventos del día seleccionado
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Eventos del ${uiState.diaSeleccionado?.format(
                                    DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES"))
                                )}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (uiState.eventosDiaSeleccionado.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No hay eventos para este día",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(uiState.eventosDiaSeleccionado) { evento ->
                                        EventoItem(
                                            evento = evento,
                                            onDelete = { viewModel.eliminarEvento(evento) }
                                        )
                                        
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Diálogo para crear un evento
        if (uiState.mostrarDialogoEvento) {
            CrearEventoDialog(
                onDismiss = { viewModel.ocultarDialogoCrearEvento() },
                onConfirm = { titulo, descripcion, tipoEvento ->
                    viewModel.crearEvento(titulo, descripcion, tipoEvento)
                }
            )
        }
    }
}

@Composable
fun DiaCalendario(
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
                    esDiaSeleccionado -> ProfesorColor
                    esHoy -> ProfesorColor.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (esHoy && !esDiaSeleccionado) 1.dp else 0.dp,
                color = if (esHoy && !esDiaSeleccionado) ProfesorColor else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
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
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

@Composable
fun EventoItem(
    evento: Evento,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono según tipo de evento
        val (color, icon) = when (evento.tipo) {
            TipoEvento.CLASE -> evento.tipo.color to Icons.Default.School
            TipoEvento.REUNION -> evento.tipo.color to Icons.Default.Group
            TipoEvento.EXAMEN -> evento.tipo.color to Icons.AutoMirrored.Filled.Assignment
            TipoEvento.EXCURSION -> evento.tipo.color to Icons.Default.DirectionsBus
            TipoEvento.FESTIVO -> evento.tipo.color to Icons.Default.Event
            TipoEvento.ESCOLAR -> evento.tipo.color to Icons.AutoMirrored.Filled.MenuBook
            TipoEvento.OTRO -> evento.tipo.color to Icons.Default.Event
        }
        
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Extraer Hora si existe en la descripción
            val descripcionParts = evento.descripcion.split("\nHora:")
            val tituloMostrar = evento.titulo
            val descripcionMostrar = descripcionParts.first().trim().ifEmpty { "Sin descripción" }
            val horaMostrar = if (descripcionParts.size > 1) "Hora: ${descripcionParts[1].trim()}" else null
            
            Text(
                text = tituloMostrar,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2, // Permitir hasta 2 líneas para el título
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = descripcionMostrar,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3, // Permitir hasta 3 líneas para la descripción
                overflow = TextOverflow.Ellipsis
            )
            
            // Mostrar la hora si existe
            if (horaMostrar != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = horaMostrar,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar evento",
                tint = Color.Red
            )
        }
    }
}

@Composable
fun CrearEventoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, TipoEvento) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipoEvento by remember { mutableStateOf(TipoEvento.CLASE) }
    var hora by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(12) }
    var selectedMinute by remember { mutableStateOf(0) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Crear nuevo evento",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tipo de evento
                Text(
                    text = "Tipo de evento",
                    style = MaterialTheme.typography.labelLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TipoEvento.values().forEach { tipo ->
                        FilterChip(
                            selected = tipo == tipoEvento,
                            onClick = { tipoEvento = tipo },
                            label = { 
                                Text(
                                    text = when(tipo) {
                                        TipoEvento.CLASE -> "Clase"
                                        TipoEvento.EXAMEN -> "Examen"
                                        TipoEvento.ESCOLAR -> "Escolar"
                                        TipoEvento.REUNION -> "Reunión"
                                        TipoEvento.FESTIVO -> "Festivo"
                                        else -> tipo.name
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = if (tipo == tipoEvento) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when(tipo) {
                                    TipoEvento.CLASE -> ProfesorColor.copy(alpha = 0.2f)
                                    TipoEvento.EXAMEN -> Color(0xFFE57373).copy(alpha = 0.2f)
                                    TipoEvento.ESCOLAR -> Color(0xFF81C784).copy(alpha = 0.2f)
                                    TipoEvento.REUNION -> Color(0xFF64B5F6).copy(alpha = 0.2f)
                                    TipoEvento.FESTIVO -> Color(0xFFFFD54F).copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                selectedLeadingIconColor = when(tipo) {
                                    TipoEvento.CLASE -> ProfesorColor
                                    TipoEvento.EXAMEN -> Color(0xFFE57373)
                                    TipoEvento.ESCOLAR -> Color(0xFF81C784)
                                    TipoEvento.REUNION -> Color(0xFF64B5F6)
                                    TipoEvento.FESTIVO -> Color(0xFFFFD54F)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Título del evento
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Descripción del evento
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selector de hora
                OutlinedTextField(
                    value = hora,
                    onValueChange = { hora = it },
                    label = { Text("Hora (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Seleccionar hora"
                            )
                        }
                    },
                    readOnly = true,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            if (titulo.isNotBlank()) {
                                val descripcionFinal = if (hora.isNotEmpty()) {
                                    "$descripcion\nHora: $hora"
                                } else {
                                    descripcion
                                }
                                onConfirm(titulo, descripcionFinal, tipoEvento)
                            }
                        },
                        enabled = titulo.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfesorColor
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo selector de hora
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                hora = String.format("%02d:%02d", hour, minute)
                showTimePicker = false
            },
            initialHour = selectedHour,
            initialMinute = selectedMinute
        )
    }
}

@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    initialHour: Int,
    initialMinute: Int
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar hora",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selector de horas y minutos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Selector de horas
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Horas", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyColumn(
                            modifier = Modifier
                                .height(150.dp)
                                .width(60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(24) { hour ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onConfirm(hour, initialMinute) }
                                        .padding(vertical = 8.dp)
                                        .background(
                                            if (hour == initialHour) 
                                                ProfesorColor.copy(alpha = 0.2f) 
                                            else 
                                                Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = hour.toString().padStart(2, '0'),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (hour == initialHour) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                    
                    // Selector de minutos
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Minutos", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyColumn(
                            modifier = Modifier
                                .height(150.dp)
                                .width(60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val minutes = listOf(0, 15, 30, 45)
                            items(minutes) { minute ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onConfirm(initialHour, minute) }
                                        .padding(vertical = 8.dp)
                                        .background(
                                            if (minute == initialMinute) 
                                                ProfesorColor.copy(alpha = 0.2f) 
                                            else 
                                                Color.Transparent,
                                            shape = RoundedCornerShape(4.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = minute.toString().padStart(2, '0'),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (minute == initialMinute) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Button(
                        onClick = { onConfirm(initialHour, initialMinute) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfesorColor
                        )
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier,
        measurePolicy = flowRowMeasurePolicy(horizontalArrangement, verticalArrangement)
    )
}

private fun flowRowMeasurePolicy(
    horizontalArrangement: Arrangement.Horizontal,
    verticalArrangement: Arrangement.Vertical
): MeasurePolicy = MeasurePolicy { measurables, constraints ->
    val rows = mutableListOf<MeasuredRow>()
    val maxWidth = constraints.maxWidth
    
    var currentRow = MeasuredRow(0)
    var rowWidth = 0
    
    measurables.forEach { measurable ->
        val placeable = measurable.measure(constraints.copy(minWidth = 0))
        
        if (rowWidth + placeable.width > maxWidth) {
            // No cabe en la fila actual, crear una nueva fila
            rows.add(currentRow)
            currentRow = MeasuredRow(placeable.height)
            rowWidth = placeable.width
            currentRow.placeables.add(placeable)
        } else {
            // Añadir a la fila actual
            rowWidth += placeable.width
            currentRow.height = maxOf(currentRow.height, placeable.height)
            currentRow.placeables.add(placeable)
        }
    }
    
    // Añadir la última fila si tiene elementos
    if (currentRow.placeables.isNotEmpty()) {
        rows.add(currentRow)
    }
    
    // Calcular la altura total y establecer las posiciones Y
    var y = 0
    rows.forEach { row ->
        row.y = y
        y += row.height
    }
    
    // Ajustar alturas según el arreglo vertical
    val totalHeight = y
    
    // Asignar posiciones X según el arreglo horizontal
    rows.forEach { row ->
        val rowWidth = row.placeables.sumOf { it.width }
        val spacing = if (row.placeables.size > 1) {
            val availableSpace = maxWidth - rowWidth
            val spaces = row.placeables.size - 1
            when (horizontalArrangement) {
                Arrangement.SpaceBetween -> if (spaces > 0) availableSpace / spaces else 0
                Arrangement.SpaceEvenly -> availableSpace / (spaces + 2)
                Arrangement.SpaceAround -> availableSpace / (spaces * 2)
                Arrangement.Center -> 0
                else -> 0
            }
        } else {
            0
        }
        
        var x = when (horizontalArrangement) {
            Arrangement.Center -> (maxWidth - rowWidth) / 2
            Arrangement.End -> maxWidth - rowWidth
            Arrangement.SpaceEvenly -> spacing
            Arrangement.SpaceAround -> spacing / 2
            else -> 0
        }
        
        row.placeables.forEach { placeable ->
            row.positions.add(Offset(x.toFloat(), 0f))
            x += placeable.width + spacing
        }
    }
    
    layout(maxWidth, totalHeight) {
        rows.forEach { row ->
            row.placeables.forEachIndexed { index, placeable ->
                val position = row.positions[index]
                placeable.place(position.x.toInt(), row.y)
            }
        }
    }
}

private data class MeasuredRow(
    var height: Int,
    val placeables: MutableList<Placeable> = mutableListOf(),
    val positions: MutableList<Offset> = mutableListOf(),
    var y: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipoEventoChip(
    tipo: TipoEvento,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val (color, icon) = when (tipo) {
        TipoEvento.CLASE -> tipo.color to Icons.Default.School
        TipoEvento.REUNION -> tipo.color to Icons.Default.Group
        TipoEvento.EXAMEN -> tipo.color to Icons.AutoMirrored.Filled.Assignment
        TipoEvento.EXCURSION -> tipo.color to Icons.Default.DirectionsBus
        TipoEvento.FESTIVO -> tipo.color to Icons.Default.Event
        TipoEvento.ESCOLAR -> tipo.color to Icons.AutoMirrored.Filled.MenuBook
        TipoEvento.OTRO -> tipo.color to Icons.Default.Event
    }
    
    // Usamos ElevatedFilterChip en lugar de FilterChip para evitar conflictos
    ElevatedFilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { 
            Text(
                text = tipo.nombre,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            ) 
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (seleccionado) MaterialTheme.colorScheme.onPrimaryContainer else color
            )
        },
        colors = FilterChipDefaults.elevatedFilterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
            selectedLeadingIconColor = color
        ),
        elevation = FilterChipDefaults.elevatedFilterChipElevation(),
        modifier = Modifier.widthIn(min = 110.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun CalendarioScreenPreview() {
    UmeEguneroTheme {
        CalendarioProfesorScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CalendarioScreenDarkPreview() {
    UmeEguneroTheme {
        CalendarioProfesorScreen(navController = rememberNavController())
    }
} 