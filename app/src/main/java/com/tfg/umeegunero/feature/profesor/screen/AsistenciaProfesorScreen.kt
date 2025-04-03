package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.R
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

enum class AsistenciaEstado(val color: Color, val texto: String) {
    PRESENTE(Color(0xFF34C759), "Presente"),
    AUSENTE(Color(0xFFFF3B30), "Ausente"),
    RETRASO(Color(0xFFFF9500), "Retraso"),
    SIN_MARCAR(Color(0xFFAEAEB2), "Sin marcar")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaProfesorScreen(
    navController: NavController,
    alumnos: List<Alumno> = emptyList()
) {
    // Estado para la fecha seleccionada
    val calendar = remember { Calendar.getInstance() }
    var selectedDate by remember { mutableStateOf(calendar.time) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    
    // Estado para la asistencia de cada alumno
    val asistenciaMap = remember {
        // Inicialmente todos los alumnos sin marcar
        mutableStateMapOf<String, AsistenciaEstado>().apply {
            alumnos.forEach { alumno ->
                put(alumno.dni, AsistenciaEstado.SIN_MARCAR)
            }
        }
    }
    
    // Mostrar la fecha seleccionada en formato "dd de MMMM de yyyy"
    val dateFormatter = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    val formattedDate = dateFormatter.format(selectedDate).replaceFirstChar { it.uppercase() }
    
    // Estado para filtros
    var filtroActual by remember { mutableStateOf<AsistenciaEstado?>(null) }
    val alumnosFiltrados = remember(alumnos, filtroActual) {
        if (filtroActual == null) {
            alumnos
        } else {
            alumnos.filter { alumno ->
                asistenciaMap[alumno.dni] == filtroActual
            }
        }
    }
    
    // Estado para búsqueda
    var searchQuery by remember { mutableStateOf("") }
    val alumnosBuscados = remember(alumnosFiltrados, searchQuery) {
        if (searchQuery.isEmpty()) {
            alumnosFiltrados
        } else {
            alumnosFiltrados.filter { alumno ->
                "${alumno.nombre} ${alumno.apellidos}".contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control de Asistencia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF34C759),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    // Guardar asistencia
                    /* Aquí iría la lógica para guardar la asistencia en la base de datos */
                },
                containerColor = Color(0xFF34C759)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Guardar asistencia",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Selector de fecha
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fecha de control",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        IconButton(onClick = { showCalendarDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Seleccionar fecha",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                // Día anterior
                                calendar.time = selectedDate
                                calendar.add(Calendar.DAY_OF_MONTH, -1)
                                selectedDate = calendar.time
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Día anterior"
                            )
                        }
                        
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        
                        IconButton(
                            onClick = {
                                // Día siguiente
                                calendar.time = selectedDate
                                calendar.add(Calendar.DAY_OF_MONTH, 1)
                                selectedDate = calendar.time
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Día siguiente"
                            )
                        }
                    }
                }
            }
            
            // Resumen de asistencia
            AsistenciaResumen(
                total = alumnos.size,
                presentes = asistenciaMap.values.count { it == AsistenciaEstado.PRESENTE },
                ausentes = asistenciaMap.values.count { it == AsistenciaEstado.AUSENTE },
                retrasos = asistenciaMap.values.count { it == AsistenciaEstado.RETRASO },
                sinMarcar = asistenciaMap.values.count { it == AsistenciaEstado.SIN_MARCAR },
                onFiltroSelected = { estado ->
                    filtroActual = if (filtroActual == estado) null else estado
                },
                filtroActual = filtroActual
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Campo de búsqueda
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Buscar alumno...") },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = { 
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar búsqueda"
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            
            // Acciones rápidas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { 
                        alumnos.forEach { alumno ->
                            asistenciaMap[alumno.dni] = AsistenciaEstado.PRESENTE
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF34C759)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Todos presentes")
                }
                
                Button(
                    onClick = { 
                        alumnos.forEach { alumno ->
                            asistenciaMap[alumno.dni] = AsistenciaEstado.SIN_MARCAR
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reiniciar")
                }
            }
            
            // Lista de alumnos
            if (alumnosBuscados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = if (searchQuery.isNotEmpty()) 
                                "No se encontraron alumnos para \"$searchQuery\"" 
                            else 
                                "No hay alumnos con el filtro seleccionado",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Text(
                    text = "Lista de alumnos (${alumnosBuscados.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(alumnosBuscados) { alumno ->
                        AsistenciaAlumnoItem(
                            alumno = alumno,
                            estadoAsistencia = asistenciaMap[alumno.dni] ?: AsistenciaEstado.SIN_MARCAR,
                            onAsistenciaChange = { nuevoEstado ->
                                asistenciaMap[alumno.dni] = nuevoEstado
                            }
                        )
                    }
                }
            }
        }
        
        // Diálogo de calendario (Aquí se implementaría un calendario real)
        if (showCalendarDialog) {
            AlertDialog(
                onDismissRequest = { showCalendarDialog = false },
                title = { Text("Seleccionar fecha") },
                text = { 
                    Text("Aquí iría un selector de calendario") 
                    // En una implementación real se usaría un DatePicker
                },
                confirmButton = {
                    TextButton(onClick = { showCalendarDialog = false }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCalendarDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun AsistenciaResumen(
    total: Int,
    presentes: Int,
    ausentes: Int,
    retrasos: Int,
    sinMarcar: Int,
    onFiltroSelected: (AsistenciaEstado) -> Unit,
    filtroActual: AsistenciaEstado?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Presentes
            EstadisticaAsistencia(
                cantidad = presentes,
                total = total,
                estado = AsistenciaEstado.PRESENTE,
                seleccionado = filtroActual == AsistenciaEstado.PRESENTE,
                onClick = { onFiltroSelected(AsistenciaEstado.PRESENTE) }
            )
            
            HorizontalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
            
            // Ausentes
            EstadisticaAsistencia(
                cantidad = ausentes,
                total = total,
                estado = AsistenciaEstado.AUSENTE,
                seleccionado = filtroActual == AsistenciaEstado.AUSENTE,
                onClick = { onFiltroSelected(AsistenciaEstado.AUSENTE) }
            )
            
            HorizontalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
            
            // Retrasos
            EstadisticaAsistencia(
                cantidad = retrasos,
                total = total,
                estado = AsistenciaEstado.RETRASO,
                seleccionado = filtroActual == AsistenciaEstado.RETRASO,
                onClick = { onFiltroSelected(AsistenciaEstado.RETRASO) }
            )
            
            HorizontalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
            
            // Sin marcar
            EstadisticaAsistencia(
                cantidad = sinMarcar,
                total = total,
                estado = AsistenciaEstado.SIN_MARCAR,
                seleccionado = filtroActual == AsistenciaEstado.SIN_MARCAR,
                onClick = { onFiltroSelected(AsistenciaEstado.SIN_MARCAR) }
            )
        }
    }
}

@Composable
fun EstadisticaAsistencia(
    cantidad: Int,
    total: Int,
    estado: AsistenciaEstado,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = if (seleccionado) 
                        estado.color
                    else 
                        estado.color.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .border(
                    width = if (seleccionado) 2.dp else 0.dp,
                    color = if (seleccionado) 
                        estado.color.copy(alpha = 0.5f)
                    else
                        Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cantidad.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (seleccionado)
                    Color.White
                else
                    estado.color
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = estado.texto,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AsistenciaAlumnoItem(
    alumno: Alumno,
    estadoAsistencia: AsistenciaEstado,
    onAsistenciaChange: (AsistenciaEstado) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del alumno
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (alumno.nombre.firstOrNull() ?: "A").toString().uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del alumno
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${alumno.nombre} ${alumno.apellidos}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(estadoAsistencia.color, CircleShape)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = estadoAsistencia.texto,
                        style = MaterialTheme.typography.bodySmall,
                        color = estadoAsistencia.color
                    )
                }
            }
            
            // Botones de asistencia
            Row {
                IconButton(
                    onClick = { onAsistenciaChange(AsistenciaEstado.PRESENTE) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (estadoAsistencia == AsistenciaEstado.PRESENTE)
                                AsistenciaEstado.PRESENTE.color
                            else
                                AsistenciaEstado.PRESENTE.color.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Presente",
                        tint = if (estadoAsistencia == AsistenciaEstado.PRESENTE)
                            Color.White
                        else
                            AsistenciaEstado.PRESENTE.color
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = { onAsistenciaChange(AsistenciaEstado.RETRASO) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (estadoAsistencia == AsistenciaEstado.RETRASO)
                                AsistenciaEstado.RETRASO.color
                            else
                                AsistenciaEstado.RETRASO.color.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Retraso",
                        tint = if (estadoAsistencia == AsistenciaEstado.RETRASO)
                            Color.White
                        else
                            AsistenciaEstado.RETRASO.color
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = { onAsistenciaChange(AsistenciaEstado.AUSENTE) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (estadoAsistencia == AsistenciaEstado.AUSENTE)
                                AsistenciaEstado.AUSENTE.color
                            else
                                AsistenciaEstado.AUSENTE.color.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Ausente",
                        tint = if (estadoAsistencia == AsistenciaEstado.AUSENTE)
                            Color.White
                        else
                            AsistenciaEstado.AUSENTE.color
                    )
                }
            }
        }
    }
}

// Datos de prueba para la preview
private val alumnosPrueba = listOf(
    Alumno(
        dni = "12345678A",
        nombre = "Miguel",
        apellidos = "García López",
        fechaNacimiento = "12/05/2018",
        alergias = emptyList(),
        observaciones = "Ninguna",
        clase = "2B"
    ),
    Alumno(
        dni = "23456789B",
        nombre = "Laura",
        apellidos = "Martínez Rodríguez",
        fechaNacimiento = "23/07/2019",
        alergias = listOf("Frutos secos"),
        observaciones = "Necesita supervisión durante el recreo",
        clase = "2B"
    ),
    Alumno(
        dni = "34567890C",
        nombre = "Carlos",
        apellidos = "Fernández Sánchez",
        fechaNacimiento = "05/11/2018",
        alergias = listOf("Lactosa"),
        observaciones = "Muy activo",
        clase = "2B"
    ),
    Alumno(
        dni = "45678901D",
        nombre = "Ana",
        apellidos = "López Gómez",
        fechaNacimiento = "17/03/2019",
        alergias = emptyList(),
        observaciones = "Tímida con nuevas personas",
        clase = "2B"
    ),
    Alumno(
        dni = "56789012E",
        nombre = "David",
        apellidos = "González Pérez",
        fechaNacimiento = "30/09/2018",
        alergias = listOf("Polen"),
        observaciones = "Le gusta leer",
        clase = "2B"
    )
)

@Preview(showBackground = true)
@Composable
fun AsistenciaProfesorScreenPreview() {
    UmeEguneroTheme {
        AsistenciaProfesorScreen(
            navController = rememberNavController(),
            alumnos = alumnosPrueba
        )
    }
} 