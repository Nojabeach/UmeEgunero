package com.tfg.umeegunero.feature.profesor.screen

import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.data.model.Comidas
import com.tfg.umeegunero.data.model.CacaControl
import com.tfg.umeegunero.data.model.NecesidadesFisiologicas
import com.tfg.umeegunero.data.model.NivelConsumo
import com.tfg.umeegunero.data.model.Observacion
import com.tfg.umeegunero.data.model.Plato
import com.tfg.umeegunero.data.model.Siesta
import com.tfg.umeegunero.data.model.TipoObservacion
import com.tfg.umeegunero.feature.profesor.viewmodel.RegistroActividadViewModel
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroActividadScreen(
    viewModel: RegistroActividadViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onRegistroGuardado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Detectar si el registro se ha guardado exitosamente
    LaunchedEffect(uiState.registroGuardado) {
        if (uiState.registroGuardado) {
            scope.launch {
                snackbarHostState.showSnackbar("Registro guardado correctamente")
                // Esperar un poco antes de navegar de vuelta
                kotlinx.coroutines.delay(1500)
                onRegistroGuardado()
            }
        }
    }

    // Detectar y mostrar errores
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
                    Text(
                        text = "Registro de Actividad",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.guardarRegistro() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfesorColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.guardarRegistro() },
                containerColor = ProfesorColor,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Guardar Registro"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Contenido principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información del alumno
                uiState.alumno?.let { alumno ->
                    AlumnoInfoCard(alumno = alumno)
                }

                // Sección de comidas
                ComidasCard(
                    comidas = uiState.comidas,
                    onUpdatePrimerPlato = viewModel::updatePrimerPlato,
                    onUpdateSegundoPlato = viewModel::updateSegundoPlato,
                    onUpdatePostre = viewModel::updatePostre
                )

                // Sección de siesta
                SiestaCard(
                    siesta = uiState.siesta,
                    onUpdateInicio = viewModel::updateSiestaInicio,
                    onUpdateFin = viewModel::updateSiestaFin,
                    context = context
                )

                // Sección de necesidades fisiológicas
                NecesidadesFisiologicasCard(
                    necesidadesFisiologicas = uiState.necesidadesFisiologicas,
                    onUpdate = viewModel::updateNecesidadesFisiologicas
                )

                // Sección de observaciones
                ObservacionesCard(
                    observaciones = uiState.observaciones,
                    nuevoMensaje = uiState.nuevoObservacionMensaje,
                    nuevoTipo = uiState.nuevoObservacionTipo,
                    onUpdateMensaje = viewModel::updateNuevoObservacionMensaje,
                    onUpdateTipo = viewModel::updateNuevoObservacionTipo,
                    onAddObservacion = viewModel::addObservacion,
                    onRemoveObservacion = viewModel::removeObservacion
                )

                // Espacio al final para que el FAB no tape el contenido
                Spacer(modifier = Modifier.height(80.dp))
            }

            // Indicador de carga
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = ProfesorColor
                    )
                }
            }
        }
    }
}

@Composable
fun AlumnoInfoCard(alumno: Alumno) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar o inicial del alumno
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alumno.nombre.firstOrNull()?.toString() ?: "A",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${alumno.nombre} ${alumno.apellidos}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "DNI: ${alumno.dni}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComidasCard(
    comidas: Comidas,
    onUpdatePrimerPlato: (String, NivelConsumo) -> Unit,
    onUpdateSegundoPlato: (String, NivelConsumo) -> Unit,
    onUpdatePostre: (String, NivelConsumo) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Fastfood,
                    contentDescription = null,
                    tint = ProfesorColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Comidas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider()

            // Primer plato
            PlatoInput(
                titulo = "Primer plato",
                plato = comidas.primerPlato,
                onUpdate = onUpdatePrimerPlato
            )

            // Segundo plato
            PlatoInput(
                titulo = "Segundo plato",
                plato = comidas.segundoPlato,
                onUpdate = onUpdateSegundoPlato
            )

            // Postre
            PlatoInput(
                titulo = "Postre",
                plato = comidas.postre,
                onUpdate = onUpdatePostre
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatoInput(
    titulo: String,
    plato: Plato,
    onUpdate: (String, NivelConsumo) -> Unit
) {
    var descripcion by remember { mutableStateOf(plato.descripcion) }
    var consumo by remember { mutableStateOf(plato.nivelConsumo) }

    // Actualizar si cambia el plato
    LaunchedEffect(plato) {
        descripcion = plato.descripcion
        consumo = plato.nivelConsumo
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        // Descripción del plato
        OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
                onUpdate(it, consumo)
            },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Selector de nivel de consumo
        Text(
            text = "Nivel de consumo:",
            style = MaterialTheme.typography.bodyMedium
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = consumo == NivelConsumo.BIEN,
                onClick = {
                    consumo = NivelConsumo.BIEN
                    onUpdate(descripcion, NivelConsumo.BIEN)
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Color.Green.copy(alpha = 0.7f),
                    activeContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Bien"
                )
                Text("Bien")
            }

            SegmentedButton(
                selected = consumo == NivelConsumo.POCO,
                onClick = {
                    consumo = NivelConsumo.POCO
                    onUpdate(descripcion, NivelConsumo.POCO)
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Color.Yellow.copy(alpha = 0.7f),
                    activeContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Poco"
                )
                Text("Poco")
            }

            SegmentedButton(
                selected = consumo == NivelConsumo.NADA,
                onClick = {
                    consumo = NivelConsumo.NADA
                    onUpdate(descripcion, NivelConsumo.NADA)
                },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Color.Red.copy(alpha = 0.7f),
                    activeContentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Nada"
                )
                Text("Nada")
            }
        }
    }
}

@Composable
fun SiestaCard(
    siesta: Siesta?,
    onUpdateInicio: (Int, Int) -> Unit,
    onUpdateFin: (Int, Int) -> Unit,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = ProfesorColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Siesta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hora de inicio
                HoraSiestaSelector(
                    titulo = "Hora de inicio",
                    timestamp = siesta?.inicio,
                    onTimeSelected = onUpdateInicio,
                    context = context,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Hora de fin
                HoraSiestaSelector(
                    titulo = "Hora de fin",
                    timestamp = siesta?.fin,
                    onTimeSelected = onUpdateFin,
                    context = context,
                    modifier = Modifier.weight(1f)
                )
            }

            // Duración de la siesta
            if (siesta != null && siesta.inicio != null && siesta.fin != null) {
                Text(
                    text = "Duración: ${siesta.duracion} minutos",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun HoraSiestaSelector(
    titulo: String,
    timestamp: Timestamp?,
    onTimeSelected: (Int, Int) -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeText = if (timestamp != null) {
        timeFormat.format(timestamp.toDate())
    } else {
        "No registrado"
    }

    val calendar = Calendar.getInstance()
    if (timestamp != null) {
        calendar.time = timestamp.toDate()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = {
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                TimePickerDialog(
                    context,
                    { _, hourOfDay, minuteOfHour ->
                        onTimeSelected(hourOfDay, minuteOfHour)
                    },
                    hour,
                    minute,
                    true
                ).show()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = ProfesorColor
            )
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = timeText)
        }
    }
}

@Composable
fun NecesidadesFisiologicasCard(
    necesidadesFisiologicas: CacaControl,
    onUpdate: (Boolean, Boolean, String) -> Unit
) {
    var pipi by remember { mutableStateOf(necesidadesFisiologicas.pipi) }
    var caca by remember { mutableStateOf(necesidadesFisiologicas.caca) }
    var observaciones by remember { mutableStateOf(necesidadesFisiologicas.observaciones) }

    // Actualizar si cambian las necesidades
    LaunchedEffect(necesidadesFisiologicas) {
        pipi = necesidadesFisiologicas.pipi
        caca = necesidadesFisiologicas.caca
        observaciones = necesidadesFisiologicas.observaciones
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Necesidades Fisiológicas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            // Opciones con checkboxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pipí
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            pipi = !pipi
                            onUpdate(caca, pipi, observaciones)
                        }
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = pipi,
                            onCheckedChange = { checked ->
                                pipi = checked
                                onUpdate(caca, checked, observaciones)
                            }
                        )
                        Text(
                            text = "Pipí",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Caca
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            caca = !caca
                            onUpdate(caca, pipi, observaciones)
                        }
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = caca,
                            onCheckedChange = { checked ->
                                caca = checked
                                onUpdate(checked, pipi, observaciones)
                            }
                        )
                        Text(
                            text = "Caca",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            
            // Campo de observaciones
            OutlinedTextField(
                value = observaciones,
                onValueChange = { 
                    observaciones = it
                    onUpdate(caca, pipi, it)
                },
                label = { Text("Observaciones") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservacionesCard(
    observaciones: List<Observacion>,
    nuevoMensaje: String,
    nuevoTipo: TipoObservacion,
    onUpdateMensaje: (String) -> Unit,
    onUpdateTipo: (TipoObservacion) -> Unit,
    onAddObservacion: () -> Unit,
    onRemoveObservacion: (Int) -> Unit
) {
    var showTipoDropdown by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = ProfesorColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Observaciones",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider()

            // Agregar nueva observación
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown para seleccionar tipo
                Box(
                    modifier = Modifier.weight(0.3f)
                ) {
                    OutlinedTextField(
                        value = nuevoTipo.name,
                        onValueChange = { /* No permitir edición directa */ },
                        label = { Text("Tipo") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showTipoDropdown = true }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Seleccionar tipo",
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = showTipoDropdown,
                        onDismissRequest = { showTipoDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        TipoObservacion.values().forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo.name) },
                                onClick = {
                                    onUpdateTipo(tipo)
                                    showTipoDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Campo para mensaje
                OutlinedTextField(
                    value = nuevoMensaje,
                    onValueChange = onUpdateMensaje,
                    label = { Text("Mensaje") },
                    modifier = Modifier.weight(0.7f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Botón para agregar
                IconButton(
                    onClick = onAddObservacion,
                    enabled = nuevoMensaje.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar observación",
                        tint = if (nuevoMensaje.isNotBlank()) ProfesorColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }

            // Lista de observaciones
            if (observaciones.isEmpty()) {
                Text(
                    text = "No hay observaciones registradas",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            } else {
                observaciones.forEachIndexed { index, observacion ->
                    ObservacionItem(
                        observacion = observacion,
                        onRemove = { onRemoveObservacion(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun ObservacionItem(
    observacion: Observacion,
    onRemove: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                // Tipo y hora
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = observacion.tipo.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTimestamp(observacion.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Botón eliminar
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar observación",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Mensaje
            Text(
                text = observacion.mensaje,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Función para formatear timestamp
fun formatTimestamp(timestamp: Timestamp): String {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return dateFormat.format(timestamp.toDate())
}

@Composable
fun HiltRegistroActividadScreen(
    viewModel: RegistroActividadViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onRegistroGuardado: () -> Unit
) {
    RegistroActividadScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        onRegistroGuardado = onRegistroGuardado
    )
}

@Preview(showBackground = true)
@Composable
fun RegistroActividadScreenPreview() {
    UmeEguneroTheme {
        val alumno = Alumno(
            dni = "12345678X",
            nombre = "Lucas",
            apellidos = "Martínez García",
            fechaNacimiento = Timestamp(Date())
        )
        
        // En lugar de crear un ViewModel real, simulamos la pantalla con datos mockeados
        Surface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                AlumnoInfoCard(alumno = alumno)
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RegistroActividadScreenDarkPreview() {
    UmeEguneroTheme(darkTheme = true) {
        Surface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                NecesidadesFisiologicasCard(
                    necesidadesFisiologicas = CacaControl(pipi = true, caca = false, observaciones = "Observación de ejemplo"),
                    onUpdate = { _, _, _ -> }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NecesidadesFisiologicasCardPreview() {
    UmeEguneroTheme {
        Surface {
            NecesidadesFisiologicasCard(
                necesidadesFisiologicas = CacaControl(pipi = true, caca = false, observaciones = "Observación de ejemplo"),
                onUpdate = { _, _, _ -> }
            )
        }
    }
}