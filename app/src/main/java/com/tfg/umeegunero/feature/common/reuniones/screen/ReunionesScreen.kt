package com.tfg.umeegunero.feature.common.reuniones.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.*
import com.tfg.umeegunero.feature.common.reuniones.viewmodel.*
import java.text.SimpleDateFormat
import java.util.*
import java.time.*
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Pantalla principal de reuniones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReunionesScreen(
    viewModel: ReunionesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Todos los permisos concedidos
        } else {
            // Algunos permisos denegados
            viewModel.showError("Se requieren permisos para acceder al calendario")
        }
    }
    
    // Verificar permisos al iniciar
    LaunchedEffect(Unit) {
        val calendarPermissions = arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )
        
        val permissionsToRequest = calendarPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reuniones") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleNuevaReunion() }) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva reunión")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.reuniones) { reunion ->
                        ReunionItem(
                            reunion = reunion,
                            onConfirmarAsistencia = { viewModel.confirmarAsistencia(reunion) },
                            onEliminar = { viewModel.showDeleteDialog(reunion) },
                            onAñadirAlCalendario = { viewModel.addReunionToCalendar(reunion) }
                        )
                    }
                }
            }

            if (uiState.showNuevaReunion) {
                NuevaReunionDialog(
                    uiState = uiState,
                    onDismiss = { viewModel.toggleNuevaReunion() },
                    onTituloChange = { viewModel.updateTitulo(it) },
                    onDescripcionChange = { viewModel.updateDescripcion(it) },
                    onFechaInicioChange = { viewModel.updateFechaInicio(it) },
                    onFechaFinChange = { viewModel.updateFechaFin(it) },
                    onHoraInicioChange = { viewModel.updateHoraInicio(it) },
                    onHoraFinChange = { viewModel.updateHoraFin(it) },
                    onTipoChange = { viewModel.updateTipo(it) },
                    onUbicacionChange = { viewModel.updateUbicacion(it) },
                    onEnlaceVirtualChange = { viewModel.updateEnlaceVirtual(it) },
                    onNotasChange = { viewModel.updateNotas(it) },
                    onGuardar = { viewModel.crearReunion() },
                    onAddRecordatorio = { tipo, tiempo -> viewModel.addRecordatorio(tipo, tiempo) },
                    onRemoveRecordatorio = { viewModel.removeRecordatorio(it) }
                )
            }

            if (uiState.showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.hideDeleteDialog() },
                    title = { Text("Eliminar reunión") },
                    text = { Text("¿Estás seguro de que quieres eliminar esta reunión?") },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.deleteReunion() }
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Cerrar")
                    }
                }
            }

            uiState.success?.let { success ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(success)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.clearSuccess() }) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

/**
 * Item de reunión
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReunionItem(
    reunion: Reunion,
    onConfirmarAsistencia: () -> Unit,
    onEliminar: () -> Unit,
    onAñadirAlCalendario: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reunion.titulo,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Inicio: ${dateFormat.format(reunion.fechaInicio.toDate())}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                ReunionChip(
                    reunion.estado,
                    modifier = Modifier.padding(4.dp)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = reunion.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Fin: ${dateFormat.format(reunion.fechaFin.toDate())}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        if (reunion.ubicacion.isNotBlank()) {
                            Text(
                                text = "Ubicación: ${reunion.ubicacion}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        if (reunion.enlaceVirtual.isNotBlank()) {
                            Text(
                                text = "Enlace: ${reunion.enlaceVirtual}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    Row {
                        IconButton(onClick = onAñadirAlCalendario) {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = "Añadir al calendario",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onConfirmarAsistencia) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Confirmar asistencia",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onEliminar) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                if (reunion.notas.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notas: ${reunion.notas}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // Visualización de recordatorios
                if (reunion.recordatorios.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recordatorios",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    reunion.recordatorios.forEach { recordatorio ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (recordatorio.tipo) {
                                    TipoRecordatorio.NOTIFICACION -> Icons.Default.Notifications
                                    TipoRecordatorio.EMAIL -> Icons.Default.Email
                                    TipoRecordatorio.AMBOS -> Icons.Default.NotificationsActive
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = when (recordatorio.estado) {
                                    EstadoRecordatorio.PENDIENTE -> MaterialTheme.colorScheme.primary
                                    EstadoRecordatorio.ENVIADO -> MaterialTheme.colorScheme.secondary
                                    EstadoRecordatorio.FALLIDO -> MaterialTheme.colorScheme.error
                                }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "${recordatorio.tiempoAntes} minutos antes",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = reunion.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Diálogo para crear una nueva reunión
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaReunionDialog(
    uiState: ReunionesUiState,
    onDismiss: () -> Unit,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onFechaInicioChange: (LocalDate) -> Unit,
    onFechaFinChange: (LocalDate) -> Unit,
    onHoraInicioChange: (LocalTime) -> Unit,
    onHoraFinChange: (LocalTime) -> Unit,
    onTipoChange: (TipoReunion) -> Unit,
    onUbicacionChange: (String) -> Unit,
    onEnlaceVirtualChange: (String) -> Unit,
    onNotasChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onAddRecordatorio: (TipoRecordatorio, Long) -> Unit,
    onRemoveRecordatorio: (Recordatorio) -> Unit
) {
    var showDatePickerInicio by remember { mutableStateOf(false) }
    var showDatePickerFin by remember { mutableStateOf(false) }
    var showTimePickerInicio by remember { mutableStateOf(false) }
    var showTimePickerFin by remember { mutableStateOf(false) }
    var showRecordatorioDialog by remember { mutableStateOf(false) }
    
    // Validación de fechas
    val fechaInicioCompleta = remember(uiState.fechaInicio, uiState.horaInicio) {
        uiState.fechaInicio.atTime(uiState.horaInicio)
    }
    
    val fechaFinCompleta = remember(uiState.fechaFin, uiState.horaFin) {
        uiState.fechaFin.atTime(uiState.horaFin)
    }
    
    val fechaInicioValida = remember(fechaInicioCompleta) {
        fechaInicioCompleta.isAfter(LocalDateTime.now())
    }
    
    val fechaFinValida = remember(fechaFinCompleta, fechaInicioCompleta) {
        fechaFinCompleta.isAfter(fechaInicioCompleta)
    }
    
    // Validaciones adicionales
    val tituloValido = remember(uiState.titulo) {
        uiState.titulo.length >= 3 && uiState.titulo.length <= 100
    }
    
    val descripcionValida = remember(uiState.descripcion) {
        uiState.descripcion.length >= 10 && uiState.descripcion.length <= 500
    }
    
    val puedeGuardar = remember(
        tituloValido,
        descripcionValida,
        fechaInicioValida,
        fechaFinValida
    ) {
        tituloValido &&
        descripcionValida &&
        fechaInicioValida &&
        fechaFinValida
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva reunión") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Título con validación
                Column {
                    OutlinedTextField(
                        value = uiState.titulo,
                        onValueChange = onTituloChange,
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = !tituloValido && uiState.titulo.isNotBlank(),
                        supportingText = {
                            if (!tituloValido && uiState.titulo.isNotBlank()) {
                                Text(
                                    text = "El título debe tener entre 3 y 100 caracteres",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }

                // Descripción con validación
                Column {
                    OutlinedTextField(
                        value = uiState.descripcion,
                        onValueChange = onDescripcionChange,
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        isError = !descripcionValida && uiState.descripcion.isNotBlank(),
                        supportingText = {
                            if (!descripcionValida && uiState.descripcion.isNotBlank()) {
                                Text(
                                    text = "La descripción debe tener entre 10 y 500 caracteres",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )
                }

                // Fecha y hora de inicio
                Column {
                    OutlinedTextField(
                        value = "${uiState.fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} ${uiState.horaInicio.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Fecha y hora de inicio") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { showDatePickerInicio = true }) {
                                    Icon(Icons.Default.DateRange, "Seleccionar fecha")
                                }
                                IconButton(onClick = { showTimePickerInicio = true }) {
                                    Icon(Icons.Default.Schedule, "Seleccionar hora")
                                }
                            }
                        },
                        isError = !fechaInicioValida
                    )
                    if (!fechaInicioValida) {
                        Text(
                            text = "La fecha de inicio debe ser posterior a la fecha actual",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }

                // Fecha y hora de fin
                Column {
                    OutlinedTextField(
                        value = "${uiState.fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} ${uiState.horaFin.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Fecha y hora de fin") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { showDatePickerFin = true }) {
                                    Icon(Icons.Default.DateRange, "Seleccionar fecha")
                                }
                                IconButton(onClick = { showTimePickerFin = true }) {
                                    Icon(Icons.Default.Schedule, "Seleccionar hora")
                                }
                            }
                        },
                        isError = !fechaFinValida
                    )
                    if (!fechaFinValida) {
                        Text(
                            text = "La fecha de fin debe ser posterior a la fecha de inicio",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }

                // Selector de tipo de reunión
                var expandedTipo by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedTipo,
                    onExpandedChange = { expandedTipo = it }
                ) {
                    OutlinedTextField(
                        value = uiState.tipo.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Tipo de reunión") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedTipo,
                        onDismissRequest = { expandedTipo = false }
                    ) {
                        TipoReunion.values().forEach { tipo ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        tipo.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                                    ) 
                                },
                                onClick = {
                                    onTipoChange(tipo)
                                    expandedTipo = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.ubicacion,
                    onValueChange = onUbicacionChange,
                    label = { Text("Ubicación") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.enlaceVirtual,
                    onValueChange = onEnlaceVirtualChange,
                    label = { Text("Enlace virtual") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.notas,
                    onValueChange = onNotasChange,
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Sección de recordatorios
                Text(
                    text = "Recordatorios",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(uiState.recordatorios) { recordatorio ->
                        RecordatorioItem(
                            recordatorio = recordatorio,
                            onDelete = { onRemoveRecordatorio(recordatorio) }
                        )
                    }
                }

                Button(
                    onClick = { showRecordatorioDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Añadir recordatorio",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir recordatorio")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onGuardar,
                enabled = puedeGuardar
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // DatePicker para fecha de inicio
    if (showDatePickerInicio) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePickerInicio = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.ofEpochMilli(millis)
                        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                        onFechaInicioChange(localDate)
                    }
                    showDatePickerInicio = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerInicio = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                title = { Text("Seleccionar fecha de inicio") },
                headline = { Text("Fecha de inicio") }
            )
        }
    }

    // DatePicker para fecha de fin
    if (showDatePickerFin) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.fechaFin.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePickerFin = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.ofEpochMilli(millis)
                        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                        onFechaFinChange(localDate)
                    }
                    showDatePickerFin = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerFin = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                title = { Text("Seleccionar fecha de fin") },
                headline = { Text("Fecha de fin") }
            )
        }
    }

    // TimePicker para hora de inicio
    if (showTimePickerInicio) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.horaInicio.hour,
            initialMinute = uiState.horaInicio.minute
        )
        
        CustomTimePickerDialog(
            onDismissRequest = { showTimePickerInicio = false },
            confirmButton = {
                TextButton(onClick = {
                    onHoraInicioChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showTimePickerInicio = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerInicio = false }) {
                    Text("Cancelar")
                }
            },
            content = {
                TimePicker(
                    state = timePickerState,
                    layoutType = TimePickerLayoutType.Vertical
                )
            }
        )
    }

    // TimePicker para hora de fin
    if (showTimePickerFin) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.horaFin.hour,
            initialMinute = uiState.horaFin.minute
        )
        
        CustomTimePickerDialog(
            onDismissRequest = { showTimePickerFin = false },
            confirmButton = {
                TextButton(onClick = {
                    onHoraFinChange(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showTimePickerFin = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerFin = false }) {
                    Text("Cancelar")
                }
            },
            content = {
                TimePicker(
                    state = timePickerState,
                    layoutType = TimePickerLayoutType.Vertical
                )
            }
        )
    }

    // Diálogo para añadir recordatorio
    if (showRecordatorioDialog) {
        var selectedTipo by remember { mutableStateOf(TipoRecordatorio.NOTIFICACION) }
        var tiempoAntes by remember { mutableStateOf("30") }
        
        AlertDialog(
            onDismissRequest = { showRecordatorioDialog = false },
            title = { Text("Añadir recordatorio") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Selector de tipo de recordatorio
                    var expandedTipo by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedTipo,
                        onExpandedChange = { expandedTipo = it }
                    ) {
                        OutlinedTextField(
                            value = selectedTipo.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Tipo de recordatorio") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expandedTipo,
                            onDismissRequest = { expandedTipo = false }
                        ) {
                            TipoRecordatorio.values().forEach { tipo ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            tipo.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                                        ) 
                                    },
                                    onClick = {
                                        selectedTipo = tipo
                                        expandedTipo = false
                                    }
                                )
                            }
                        }
                    }

                    // Campo para el tiempo antes
                    OutlinedTextField(
                        value = tiempoAntes,
                        onValueChange = { tiempoAntes = it },
                        label = { Text("Minutos antes") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        tiempoAntes.toLongOrNull()?.let { tiempo ->
                            onAddRecordatorio(selectedTipo, tiempo)
                        }
                        showRecordatorioDialog = false
                    }
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordatorioDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    content()
                }
            }
        },
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

@Composable
fun ReunionChip(
    estado: EstadoReunion,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { },
        label = { Text(estado.name) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = when (estado) {
                EstadoReunion.PROGRAMADA -> MaterialTheme.colorScheme.primaryContainer
                EstadoReunion.CANCELADA -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            labelColor = when (estado) {
                EstadoReunion.PROGRAMADA -> MaterialTheme.colorScheme.onPrimaryContainer
                EstadoReunion.CANCELADA -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        modifier = modifier
    )
}

@Composable
fun RecordatorioItem(
    recordatorio: Recordatorio,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = recordatorio.tipo.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${recordatorio.tiempoAntes} minutos antes",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar recordatorio",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipoReunionDropdown(
    selectedTipo: TipoReunion,
    onTipoSelected: (TipoReunion) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedTipo.name,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TipoReunion.values().forEach { tipo ->
                DropdownMenuItem(
                    text = { Text(tipo.name) },
                    onClick = {
                        onTipoSelected(tipo)
                        expanded = false
                    }
                )
            }
        }
    }
} 