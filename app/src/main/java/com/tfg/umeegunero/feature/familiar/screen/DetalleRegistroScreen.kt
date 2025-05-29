package com.tfg.umeegunero.feature.familiar.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Actividad
import com.tfg.umeegunero.data.model.CacaControl
import com.tfg.umeegunero.data.model.Comida
import com.tfg.umeegunero.data.model.Comidas
import com.tfg.umeegunero.data.model.EstadoComida
import com.tfg.umeegunero.data.model.NotificacionAusencia
import com.tfg.umeegunero.data.model.RegistroActividad
import com.tfg.umeegunero.data.model.Siesta
import com.tfg.umeegunero.feature.familiar.viewmodel.DetalleRegistroViewModel
import com.tfg.umeegunero.feature.familiar.viewmodel.DetalleRegistroUiState
import com.tfg.umeegunero.ui.theme.FamiliarColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Pantalla de detalle de registro de actividad diaria
 * 
 * Muestra la información completa de un registro de actividad de un alumno,
 * incluyendo alimentación, siesta, higiene, observaciones generales y ausencias notificadas.
 * 
 * @param registroId ID del registro a mostrar
 * @param navController Controlador de navegación
 * @param viewModel ViewModel que gestiona el estado de la pantalla
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRegistroScreen(
    registroId: String,
    navController: NavController,
    viewModel: DetalleRegistroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Recargar el registro cuando cambie la fecha seleccionada
    LaunchedEffect(uiState.fechaSeleccionada) {
        Timber.d("[DetalleRegistroScreen] Fecha seleccionada cambió: ${uiState.fechaSeleccionada}")
        viewModel.seleccionarFecha(uiState.fechaSeleccionada)
    }

    // Observar cambios en el registro cuando cambia la fecha
    LaunchedEffect(uiState.registro?.id) {
        uiState.registro?.let { nuevoRegistro ->
            Timber.d("Registro actualizado: ${nuevoRegistro.id}, fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(nuevoRegistro.fecha.toDate())}")
        }
    }

    // Mostrar error si existe
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Actividad diaria",
                            color = Color.White
                        )
                        if (uiState.alumnoNombre != null) {
                            Text(
                                text = uiState.alumnoNombre ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver atrás",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Selector de fecha si hay fechas disponibles
                    if (uiState.registrosDisponibles.isNotEmpty()) {
                        IconButton(
                            onClick = { 
                                try {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleSelectorFecha() 
                                } catch (e: Exception) {
                                    Timber.e(e, "Error en feedback háptico: ${e.message}")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Seleccionar fecha",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FamiliarColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FamiliarColor)
                }
            } else if (uiState.registro == null && uiState.ausenciaNotificada == null) {
                // Mostrar mensaje de error si no hay registro ni ausencia
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No se encontró información para esta fecha",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Volver al listado")
                        }
                    }
                }
            } else {
                // Mostrar contenido
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Selector de fecha
                    item {
                        FechaSelectorCard(
                            fechaSeleccionada = uiState.fechaSeleccionada,
                            fechasDisponibles = uiState.registrosDisponibles,
                            mostrarSelector = uiState.mostrarSelectorFecha,
                            onFechaSeleccionada = { 
                                Timber.d("Seleccionando nueva fecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)}")
                                viewModel.seleccionarFecha(it) 
                            },
                            onToggleSelector = { viewModel.toggleSelectorFecha() },
                            onHaptic = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                        )
                    }
                    
                    // Si hay ausencia notificada, mostrarla primero
                    uiState.ausenciaNotificada?.let { ausencia ->
                        item {
                            AusenciaNotificadaCard(ausencia = ausencia)
                        }
                    }
                    
                    // Si hay registro, mostrar sus detalles
                    uiState.registro?.let { registro ->
                        item {
                            RegistroActividadCard(
                                registro = registro,
                                profesorNombre = uiState.profesorNombre
                            )
                        }
                    }
                }
            }
            
            // Dropdown de selección de fecha (se muestra sobre todo el contenido)
            if (uiState.mostrarSelectorFecha) {
                FechaSelectorDropdown(
                    fechaSeleccionada = uiState.fechaSeleccionada,
                    fechasDisponibles = uiState.registrosDisponibles,
                    onFechaSeleccionada = { 
                        Timber.d("Fecha seleccionada desde dropdown: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)}")
                        viewModel.seleccionarFecha(it) 
                    },
                    onDismiss = { viewModel.toggleSelectorFecha() }
                )
            }
        }
    }
}

/**
 * Tarjeta para mostrar una ausencia notificada
 */
@Composable
fun AusenciaNotificadaCard(
    ausencia: NotificacionAusencia,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ausencia notificada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(ausencia.fechaNotificacion.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Motivo
            Text(
                text = "Motivo: ${ausencia.motivo}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            // Comentarios del profesor si los hay
            if (ausencia.comentariosProfesor.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Comentarios del profesor: ${ausencia.comentariosProfesor}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            // Notificado por
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Notificado por: ${ausencia.familiarNombre}",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * Tarjeta para mostrar un registro de actividad completo
 */
@Composable
fun RegistroActividadCard(
    registro: RegistroActividad,
    profesorNombre: String? = null,
    modifier: Modifier = Modifier
) {
    val fecha = SimpleDateFormat("HH:mm", Locale.getDefault()).format(registro.fecha.toDate())
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado con fecha y hora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registro de actividades",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Registrado a las $fecha",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Profesor que registró
            if (profesorNombre != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Por: $profesorNombre",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
            
            // Indicador de lectura por familiares
            if (registro.lecturasPorFamiliar.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoSeccion(
                    icon = Icons.Default.CheckCircle,
                    title = "Estado de lectura",
                    content = {
                        LecturaFamiliaresIndicador(
                            lecturasPorFamiliar = registro.lecturasPorFamiliar
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
                tint = FamiliarColor,
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

@Composable
fun LecturaFamiliaresIndicador(
    lecturasPorFamiliar: Map<String, com.tfg.umeegunero.data.model.LecturaFamiliar>
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { mostrarDialogo = true }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = "Lecturas",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Leído por ${lecturasPorFamiliar.size} familiar(es)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Ver detalles",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        
        // Mostrar nombres de familiares que han leído
        lecturasPorFamiliar.values.take(2).forEach { lectura ->
            Text(
                text = "• Familiar ${lectura.familiarId.take(4)}...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 28.dp, top = 2.dp)
            )
        }
        
        if (lecturasPorFamiliar.size > 2) {
            Text(
                text = "• y ${lecturasPorFamiliar.size - 2} más...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 28.dp, top = 2.dp)
            )
        }
    }
    
    // Diálogo con detalles de lecturas
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = {
                Text(
                    text = "Detalles de lectura",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn {
                    items(lecturasPorFamiliar.values.toList()) { lectura ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lectura.familiarId,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Leído",
                                        tint = Color.Green,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                val fechaLectura = SimpleDateFormat(
                                    "dd/MM/yyyy 'a las' HH:mm",
                                    Locale.getDefault()
                                ).format(lectura.fechaLectura.toDate())
                                
                                Text(
                                    text = "Leído el $fechaLectura",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

/**
 * Tarjeta para mostrar la fecha seleccionada y permitir cambiarla
 */
@Composable
fun FechaSelectorCard(
    fechaSeleccionada: Date,
    fechasDisponibles: List<Date>,
    mostrarSelector: Boolean,
    onFechaSeleccionada: (Date) -> Unit,
    onToggleSelector: () -> Unit,
    onHaptic: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    val formattedDate = dateFormat.format(fechaSeleccionada).replaceFirstChar { it.uppercase() }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    if (isPressed) {
        onHaptic()
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (fechasDisponibles.size > 1) {
                    onToggleSelector()
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Fecha",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            if (fechasDisponibles.size > 1) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Cambiar fecha",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Dropdown para seleccionar entre las fechas disponibles
 */
@Composable
fun FechaSelectorDropdown(
    fechaSeleccionada: Date,
    fechasDisponibles: List<Date>,
    onFechaSeleccionada: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("es", "ES"))
    val dayFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Selecciona una fecha",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                HorizontalDivider()
                
                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    fechasDisponibles.sortedByDescending { it.time }.forEach { fecha ->
                        val isSelected = fechaSeleccionada.time == fecha.time
                        val formattedDate = dateFormat.format(fecha).replaceFirstChar { it.uppercase() }
                        val shortDate = dayFormat.format(fecha)
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onFechaSeleccionada(fecha)
                                    onDismiss() // Cerrar el selector automáticamente tras seleccionar
                                },
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = formattedDate,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    
                                    Text(
                                        text = shortDate,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Seleccionado",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        HorizontalDivider()
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

// Funciones utilitarias
fun formatDateExtended(timestamp: Timestamp): String {
    val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    return dateFormat.format(timestamp.toDate()).replaceFirstChar { it.uppercase() }
}

fun formatTime(timestamp: Timestamp): String {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(timestamp.toDate())
}

fun formatDuracion(minutos: Int): String {
    val horas = minutos / 60
    val minutosRestantes = minutos % 60

    return if (horas > 0) {
        "$horas h $minutosRestantes min"
    } else {
        "$minutosRestantes min"
    }
}

/**
 * Calcula la duración en minutos entre dos horas en formato "HH:mm"
 */
fun calcularDuracionSiesta(horaInicio: String, horaFin: String): Int {
    if (horaInicio.isBlank() || horaFin.isBlank()) return 0
    
    try {
        val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
        val inicio = formatoHora.parse(horaInicio)
        val fin = formatoHora.parse(horaFin)
        
        if (inicio != null && fin != null) {
            // Calcular la diferencia en minutos
            val diferenciaMillis = fin.time - inicio.time
            return (diferenciaMillis / (1000 * 60)).toInt()
        }
    } catch (e: Exception) {
        return 0
    }
    
    return 0
}

// Función para determinar si una fecha es hoy
private fun esHoy(fecha: Date): Boolean {
    val hoy = Calendar.getInstance()
    val cal = Calendar.getInstance()
    cal.time = fecha
    
    return hoy.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
           hoy.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
}

// Función para determinar si una fecha es ayer
private fun esAyer(fecha: Date): Boolean {
    val hoy = Calendar.getInstance()
    val ayer = Calendar.getInstance()
    ayer.add(Calendar.DAY_OF_YEAR, -1)
    
    val cal = Calendar.getInstance()
    cal.time = fecha
    
    return ayer.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
           ayer.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
}