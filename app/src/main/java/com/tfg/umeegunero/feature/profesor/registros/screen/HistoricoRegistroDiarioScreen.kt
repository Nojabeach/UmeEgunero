package com.tfg.umeegunero.feature.profesor.registros.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.feature.profesor.registros.viewmodel.HistoricoRegistroDiarioViewModel
import com.tfg.umeegunero.ui.components.calendar.DateSelector
import com.tfg.umeegunero.ui.theme.ProfesorColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoRegistroDiarioScreen(
    navController: NavController,
    viewModel: HistoricoRegistroDiarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Estados para los menús desplegables
    var mostrarMenuClase by remember { mutableStateOf(false) }
    var mostrarMenuAlumno by remember { mutableStateOf(false) }
    var mostrarCalendario by remember { mutableStateOf(false) }
    
    // Efecto para mostrar errores
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.limpiarError()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Registros Diarios") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refrescarDatos() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfesorColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Filtros
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Filtros de búsqueda",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Selector de clase
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mostrarMenuClase = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Class,
                                    contentDescription = null,
                                    tint = ProfesorColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.claseSeleccionada?.nombre ?: "Seleccionar clase",
                                    modifier = Modifier.weight(1f),
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = mostrarMenuClase,
                            onDismissRequest = { mostrarMenuClase = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            uiState.clases.forEach { clase ->
                                DropdownMenuItem(
                                    text = { Text(clase.nombre) },
                                    onClick = {
                                        viewModel.seleccionarClase(clase.id)
                                        mostrarMenuClase = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Class,
                                            contentDescription = null,
                                            tint = ProfesorColor
                                        )
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Selector de alumno
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (uiState.alumnos.isNotEmpty()) {
                                        mostrarMenuAlumno = true 
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = ProfesorColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.alumnoSeleccionado != null) {
                                        "${uiState.alumnoSeleccionado?.nombre} ${uiState.alumnoSeleccionado?.apellidos}"
                                    } else {
                                        "Seleccionar alumno"
                                    },
                                    modifier = Modifier.weight(1f),
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = mostrarMenuAlumno,
                            onDismissRequest = { mostrarMenuAlumno = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            uiState.alumnos.forEach { alumno ->
                                DropdownMenuItem(
                                    text = { Text("${alumno.nombre} ${alumno.apellidos}") },
                                    onClick = {
                                        viewModel.seleccionarAlumno(alumno.id)
                                        mostrarMenuAlumno = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = ProfesorColor
                                        )
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Selector de fecha
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mostrarCalendario = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = ProfesorColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                        .format(uiState.fechaSeleccionada),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                    
                    if (mostrarCalendario) {
                        DateSelector(
                            selectedDate = uiState.fechaSeleccionada,
                            onDateSelected = { fecha -> 
                                viewModel.seleccionarFecha(fecha)
                                mostrarCalendario = false
                            },
                            onDismissRequest = { mostrarCalendario = false }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de registros
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ProfesorColor)
                }
            } else if (uiState.registros.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = ProfesorColor.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.alumnoSeleccionado != null) {
                                "No hay registros para ${uiState.alumnoSeleccionado?.nombre} en la fecha seleccionada"
                            } else {
                                "Selecciona una clase y un alumno para ver sus registros"
                            },
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    items(uiState.registros) { registro ->
                        RegistroDiarioCard(registro = registro)
                    }
                }
            }
        }
    }
}

@Composable
fun RegistroDiarioCard(
    registro: RegistroActividad,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                    tint = ProfesorColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(registro.fecha.toDate()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resumen de comidas
            InfoSection(
                icon = Icons.Default.LocalDining,
                title = "Comidas",
                content = obtenerResumenComidas(registro)
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Siesta
            InfoSection(
                icon = Icons.Default.NightsStay,
                title = "Siesta",
                content = if (registro.haSiestaSiNo) "Ha dormido siesta" else "No ha dormido siesta"
            )
            
            if (registro.observacionesSiesta?.isNotEmpty() == true) {
                Text(
                    text = registro.observacionesSiesta ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Deposiciones
            InfoSection(
                icon = Icons.Default.Wc,
                title = "Deposiciones",
                content = if (registro.haHechoCaca) {
                    "Ha hecho caca ${registro.numeroCacas} ${if (registro.numeroCacas == 1) "vez" else "veces"}"
                } else {
                    "No ha hecho caca"
                }
            )
            
            if (registro.observacionesCaca?.isNotEmpty() == true) {
                Text(
                    text = registro.observacionesCaca ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
            
            // Material necesario
            if (registro.necesitaPanales || registro.necesitaToallitas || registro.necesitaRopaCambio || 
                !registro.otroMaterialNecesario.isNullOrEmpty()) {
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                Text(
                    text = "Material necesario:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 32.dp)
                )
                
                Column(modifier = Modifier.padding(start = 32.dp, top = 4.dp)) {
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
            
            // Observaciones generales
            if (!registro.observacionesGenerales.isNullOrEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                InfoSection(
                    icon = Icons.Default.Subject,
                    title = "Observaciones generales",
                    content = ""
                )
                
                Text(
                    text = registro.observacionesGenerales ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 32.dp)
                )
            }
        }
    }
}

@Composable
fun InfoSection(
    icon: ImageVector,
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(top = 3.dp),
            tint = ProfesorColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            if (content.isNotEmpty()) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Obtiene un resumen textual del estado de las comidas
 */
private fun obtenerResumenComidas(registro: RegistroActividad): String {
    val comidas = mutableListOf<String>()
    
    if (registro.primerPlato != null && registro.primerPlato != EstadoComida.NO_SERVIDO) {
        comidas.add("Primer plato: ${obtenerTextoEstadoComida(registro.primerPlato!!)}")
    }
    
    if (registro.segundoPlato != null && registro.segundoPlato != EstadoComida.NO_SERVIDO) {
        comidas.add("Segundo plato: ${obtenerTextoEstadoComida(registro.segundoPlato!!)}")
    }
    
    if (registro.postre != null && registro.postre != EstadoComida.NO_SERVIDO) {
        comidas.add("Postre: ${obtenerTextoEstadoComida(registro.postre!!)}")
    }
    
    if (registro.merienda != null && registro.merienda != EstadoComida.NO_SERVIDO) {
        comidas.add("Merienda: ${obtenerTextoEstadoComida(registro.merienda!!)}")
    }
    
    return if (comidas.isEmpty()) {
        "No se ha servido ninguna comida"
    } else {
        comidas.joinToString(", ")
    }
}

/**
 * Convierte el estado de comida a texto legible
 */
private fun obtenerTextoEstadoComida(estado: EstadoComida): String {
    return when (estado) {
        EstadoComida.COMPLETO -> "Completo"
        EstadoComida.PARCIAL -> "Parcial"
        EstadoComida.RECHAZADO -> "Rechazado"
        EstadoComida.NO_SERVIDO -> "No servido"
        EstadoComida.SIN_DATOS -> "Sin datos"
        EstadoComida.NO_APLICABLE -> "No aplicable"
    }
} 