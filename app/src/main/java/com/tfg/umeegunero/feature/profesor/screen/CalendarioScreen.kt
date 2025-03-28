package com.tfg.umeegunero.feature.profesor.screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Evento
import com.tfg.umeegunero.data.model.TipoEvento
import com.tfg.umeegunero.feature.profesor.viewmodel.CalendarioViewModel
import com.tfg.umeegunero.ui.theme.ProfesorColor
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

/**
 * Pantalla de calendario para profesores
 * Permite visualizar y gestionar eventos escolares
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
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
            Text(
                text = evento.titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = evento.descripcion.ifEmpty { "Sin descripción" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
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
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Crear nuevo evento",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tipo de evento
                Column {
                    Text(
                        text = "Tipo de evento",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TipoEvento.values().forEach { tipo ->
                            TipoEventoChip(
                                tipo = tipo,
                                seleccionado = tipo == tipoEvento,
                                onClick = { tipoEvento = tipo }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Título
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (titulo.isNotBlank()) {
                                onConfirm(titulo, descripcion, tipoEvento)
                            }
                        },
                        enabled = titulo.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

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
    
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(tipo.nombre) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (seleccionado) MaterialTheme.colorScheme.onPrimaryContainer else color
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = MaterialTheme.colorScheme.onSurface,
            selectedLeadingIconColor = color
        )
    )
}

@Preview(showBackground = true)
@Composable
fun CalendarioScreenPreview() {
    UmeEguneroTheme {
        CalendarioScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CalendarioScreenDarkPreview() {
    UmeEguneroTheme {
        CalendarioScreen(navController = rememberNavController())
    }
} 