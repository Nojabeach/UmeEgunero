package com.tfg.umeegunero.feature.familiar.registros.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.feature.familiar.registros.viewmodel.ConsultaRegistroDiarioViewModel
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultaRegistroDiarioScreen(
    viewModel: ConsultaRegistroDiarioViewModel,
    alumnoId: String,
    alumnoNombre: String,
    onNavigateBack: () -> Unit,
    registroId: String? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var fechaSeleccionada by remember { mutableStateOf<Date?>(null) }
    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    
    // Filtrar registros por fecha si hay una fecha seleccionada
    val registrosFiltrados = remember(uiState.registros, fechaSeleccionada) {
        if (fechaSeleccionada != null) {
            // Crear un Calendar para comparar solo por fecha (sin hora)
            val calSeleccionado = Calendar.getInstance().apply {
                time = fechaSeleccionada!!
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            // Filtrar registros que coinciden con la fecha seleccionada
            uiState.registros.filter { registro ->
                val calRegistro = Calendar.getInstance().apply {
                    time = registro.fecha.toDate()
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                calSeleccionado.timeInMillis == calRegistro.timeInMillis
            }
        } else {
            uiState.registros
        }
    }
    
    // Efecto para cargar datos al iniciar la pantalla
    LaunchedEffect(alumnoId, registroId, fechaSeleccionada) {
        if (registroId != null) {
            // Si tenemos un ID específico, cargamos ese registro
            viewModel.cargarRegistroPorId(registroId)
        } else if (fechaSeleccionada != null) {
            // Si tenemos fecha seleccionada, cargamos registros de esa fecha
            viewModel.cargarRegistrosPorFecha(alumnoId, fechaSeleccionada!!)
        } else {
            // Si no, cargamos todos los registros del alumno
            viewModel.cargarRegistros(alumnoId)
        }
    }
    
    // Diálogo para seleccionar fecha
    if (mostrarSelectorFecha) {
        DatePickerDialog(
            onDismissRequest = { mostrarSelectorFecha = false },
            onDateSelected = { fecha ->
                fechaSeleccionada = fecha
                mostrarSelectorFecha = false
                // Cargar registros por la fecha seleccionada
                viewModel.cargarRegistrosPorFecha(alumnoId, fecha)
            },
            fechaActual = fechaSeleccionada ?: Date()
        )
    }
    
    // Determinar el nombre a mostrar (del estado o del parámetro)
    val nombreMostrado = if (uiState.alumnoNombre.isNotEmpty()) {
        uiState.alumnoNombre
    } else {
        alumnoNombre
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registros de $nombreMostrado") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón para filtrar por fecha
                    IconButton(onClick = { mostrarSelectorFecha = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Filtrar por fecha"
                        )
                    }
                    
                    // Si hay una fecha seleccionada, mostrar botón para limpiar filtro
                    if (fechaSeleccionada != null) {
                        IconButton(
                            onClick = { 
                                fechaSeleccionada = null
                                viewModel.cargarRegistros(alumnoId)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar filtro"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mostrar la fecha seleccionada si hay filtro activo
            if (fechaSeleccionada != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mostrando registros del ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(fechaSeleccionada!!)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            // Si tenemos un registro específico cargado, mostrarlo
            if (uiState.registroSeleccionado != null && registroId != null) {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        RegistroDiarioCard(
                            registro = uiState.registroSeleccionado!!,
                            onClick = { viewModel.marcarComoVisto(uiState.registroSeleccionado!!.id) }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            } else if (uiState.isLoading && uiState.registros.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null && uiState.registros.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${uiState.error}")
                }
            } else if (registrosFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = if (fechaSeleccionada != null) {
                                "No hay registros para la fecha seleccionada"
                            } else {
                                "No hay registros disponibles"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        
                        if (fechaSeleccionada != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = { 
                                    fechaSeleccionada = null
                                    viewModel.cargarRegistros(alumnoId)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Ver todos los registros")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(registrosFiltrados) { registro ->
                        RegistroDiarioCard(
                            registro = registro,
                            onClick = { viewModel.marcarComoVisto(registro.id) }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RegistroDiarioCard(
    registro: RegistroActividad,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (!registro.vistoPorFamiliar) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Cabecera con fecha y hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(registro.fecha.toDate()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault())
                                .format(registro.fecha.toDate()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (!registro.vistoPorFamiliar) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            
            val createdByText = registro.creadoPor?.let { "Por: $it" } ?: ""
            if (createdByText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = createdByText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Comidas
            InfoSeccion(
                icon = Icons.Default.LocalDining,
                title = "Comidas",
                content = {
                    Column {
                        ComidaItem("Primer plato", registro.comidas.primerPlato.estadoComida)
                        ComidaItem("Segundo plato", registro.comidas.segundoPlato.estadoComida)
                        ComidaItem("Postre", registro.comidas.postre.estadoComida)
                        
                        if (!registro.observacionesComida.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Observaciones: ${registro.observacionesComida}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            
            // Siesta
            InfoSeccion(
                icon = Icons.Default.NightsStay,
                title = "Siesta",
                content = {
                    Column {
                        if (registro.haSiestaSiNo) {
                            val horaInicio = registro.horaInicioSiesta.ifEmpty { "No registrada" }
                            val horaFin = registro.horaFinSiesta.ifEmpty { "No registrada" }
                            
                            Text("El alumno ha dormido siesta", style = MaterialTheme.typography.bodyMedium)
                            Text("De $horaInicio a $horaFin", style = MaterialTheme.typography.bodyMedium)
                            
                            if (!registro.observacionesSiesta.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Observaciones: ${registro.observacionesSiesta}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        } else {
                            Text("El alumno no ha dormido siesta", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            )
            
            // Mostrar deposiciones si ha hecho caca
            if (registro.haHechoCaca) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoSeccion(
                    icon = Icons.Default.Wc,
                    title = "Deposiciones",
                    content = {
                        Column {
                            Text(
                                "El alumno ha hecho ${registro.numeroCacas} deposiciones", 
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            if (!registro.observacionesCaca.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Observaciones: ${registro.observacionesCaca}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                )
            }
            
            // Materiales necesarios
            if (registro.necesitaPanales || registro.necesitaToallitas || registro.necesitaRopaCambio || 
                !registro.otroMaterialNecesario.isNullOrEmpty()) {
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoSeccion(
                    icon = Icons.Default.DateRange,
                    title = "Material necesario",
                    content = {
                        Column {
                            if (registro.necesitaPanales) {
                                Text("• Pañales", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (registro.necesitaToallitas) {
                                Text("• Toallitas", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (registro.necesitaRopaCambio) {
                                Text("• Ropa de cambio", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (!registro.otroMaterialNecesario.isNullOrEmpty()) {
                                Text("• ${registro.otroMaterialNecesario}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                )
            }
            
            // Observaciones generales
            if (!registro.observacionesGenerales.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoSeccion(
                    icon = Icons.AutoMirrored.Filled.Subject,
                    title = "Observaciones generales",
                    content = {
                        Text(
                            text = registro.observacionesGenerales ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun InfoSeccion(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Box(modifier = Modifier.padding(start = 28.dp)) {
            content()
        }
    }
}

@Composable
fun ComidaItem(nombre: String, estado: EstadoComida) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (color, texto) = when (estado) {
            EstadoComida.COMPLETO -> Pair(Color.Green, "Completo")
            EstadoComida.PARCIAL -> Pair(Color.Yellow, "Parcial")
            EstadoComida.RECHAZADO -> Pair(Color.Red, "Rechazado")
            EstadoComida.NO_SERVIDO -> Pair(Color.Gray, "No servido")
            EstadoComida.NO_APLICABLE -> Pair(Color.Gray, "No aplicable")
            EstadoComida.SIN_DATOS -> Pair(Color.Gray, "Sin datos")
        }
        
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$nombre: $texto",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Función para mantener compatibilidad con componentes existentes
 */
@Composable
fun InfoRow(
    icon: ImageVector,
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Diálogo para seleccionar una fecha
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Date) -> Unit,
    fechaActual: Date
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = fechaActual.time
    )
    
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Selecciona una fecha",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DatePicker(
                    state = datePickerState,
                    title = null, // No mostrar título duplicado
                    headline = null // No mostrar título duplicado
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancelar")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                onDateSelected(Date(millis))
                            }
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}

@Composable
fun HiltConsultaRegistroDiarioScreen(
    viewModel: ConsultaRegistroDiarioViewModel = hiltViewModel(),
    alumnoId: String,
    alumnoNombre: String,
    registroId: String? = null,
    onNavigateBack: () -> Unit
) {
    // No necesitamos un LaunchedEffect aquí, ya que el componente ConsultaRegistroDiarioScreen
    // tiene su propio LaunchedEffect que gestiona la carga de datos basándose en los parámetros
    
    ConsultaRegistroDiarioScreen(
        viewModel = viewModel,
        alumnoId = alumnoId,
        alumnoNombre = alumnoNombre,
        registroId = registroId,
        onNavigateBack = onNavigateBack
    )
}

@Preview(showBackground = true)
@Composable
fun ConsultaRegistroDiarioScreenPreview() {
    UmeEguneroTheme {
        ConsultaRegistroDiarioScreen(
            viewModel = hiltViewModel(),
            alumnoId = "1",
            alumnoNombre = "Juan Pérez",
            onNavigateBack = {}
        )
    }
} 