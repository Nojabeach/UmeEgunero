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
import androidx.compose.material.icons.filled.Subject
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
    
    LaunchedEffect(alumnoId) {
        viewModel.cargarRegistros(alumnoId)
    }
    
    // Diálogo para seleccionar fecha
    if (mostrarSelectorFecha) {
        DatePickerDialog(
            onDismissRequest = { mostrarSelectorFecha = false },
            onDateSelected = { fecha ->
                fechaSeleccionada = fecha
                mostrarSelectorFecha = false
            },
            fechaActual = fechaSeleccionada ?: Date()
        )
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Registros de $alumnoNombre") },
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
                        IconButton(onClick = { fechaSeleccionada = null }) {
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
            
            if (uiState.isLoading && uiState.registros.isEmpty()) {
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
                                onClick = { fechaSeleccionada = null },
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
            // Cabecera con fecha
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(registro.fecha.toDate()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (!registro.vistoPorFamiliar) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resumen de comidas
            InfoRow(
                icon = Icons.Default.LocalDining,
                title = "Comidas",
                content = obtenerResumenComidas(registro)
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Siesta
            InfoRow(
                icon = Icons.Default.NightsStay,
                title = "Siesta",
                content = if (registro.haSiestaSiNo) {
                    val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
                    if (registro.horaInicioSiesta.isNotEmpty() && registro.horaFinSiesta.isNotEmpty()) {
                        "De ${registro.horaInicioSiesta} a ${registro.horaFinSiesta}"
                    } else {
                        "Ha dormido siesta"
                    }
                } else {
                    "No ha dormido siesta"
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Necesidades fisiológicas
            InfoRow(
                icon = Icons.Default.Wc,
                title = "Necesidades",
                content = if (registro.haHechoCaca) {
                    "Ha hecho caca"
                } else {
                    "No ha hecho caca"
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Observaciones
            InfoRow(
                icon = Icons.Default.Subject,
                title = "Observaciones",
                content = if (registro.observacionesGenerales.isNotBlank()) {
                    registro.observacionesGenerales
                } else {
                    "No hay observaciones"
                }
            )
        }
    }
}

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
 * Obtiene un resumen de las comidas
 */
fun obtenerResumenComidas(registro: RegistroActividad): String {
    val comidas = StringBuilder()
    
    if (registro.comidas.primerPlato.estadoComida != EstadoComida.NO_SERVIDO) {
        val estado = if (registro.comidas.primerPlato.estadoComida == EstadoComida.COMPLETO) 
            "Completo" 
        else 
            "Parcial"
        comidas.append("Primer plato: $estado")
    }
    
    if (registro.comidas.segundoPlato.estadoComida != EstadoComida.NO_SERVIDO) {
        if (comidas.isNotEmpty()) comidas.append(", ")
        val estado = if (registro.comidas.segundoPlato.estadoComida == EstadoComida.COMPLETO) 
            "Completo" 
        else 
            "Parcial"
        comidas.append("Segundo plato: $estado")
    }
    
    if (registro.comidas.postre.estadoComida != EstadoComida.NO_SERVIDO) {
        if (comidas.isNotEmpty()) comidas.append(", ")
        val estado = if (registro.comidas.postre.estadoComida == EstadoComida.COMPLETO) 
            "Completo" 
        else 
            "Parcial"
        comidas.append("Postre: $estado")
    }
    
    return if (comidas.isNotEmpty()) comidas.toString() else "No ha comido"
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
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(alumnoId) {
        viewModel.cargarRegistros(alumnoId)
    }
    
    ConsultaRegistroDiarioScreen(
        viewModel = viewModel,
        alumnoId = alumnoId,
        alumnoNombre = alumnoNombre,
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