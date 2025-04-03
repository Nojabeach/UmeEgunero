package com.tfg.umeegunero.feature.profesor.screen

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.ui.theme.UmeEguneroTheme
import java.text.SimpleDateFormat
import java.util.*

// Modelo para representar una evaluación
data class Evaluacion(
    val id: String,
    val alumnoId: String,
    val trimestre: Int,
    val asignatura: String,
    val calificacion: Float,
    val comentarios: String,
    val fecha: Date
)

// Modelo para representar una asignatura
data class Asignatura(
    val id: String,
    val nombre: String,
    val color: Color
)

// Enum para representar los trimestres
enum class Trimestre(val nombre: String, val fechaInicio: String, val fechaFin: String) {
    PRIMERO("1er Trimestre", "1 sep", "15 dic"),
    SEGUNDO("2º Trimestre", "16 dic", "31 mar"),
    TERCERO("3er Trimestre", "1 abr", "30 jun")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluacionScreen(
    navController: NavController,
    alumnos: List<Alumno> = emptyList()
) {
    val asignaturas = remember {
        listOf(
            Asignatura("1", "Matemáticas", Color(0xFF5856D6)),
            Asignatura("2", "Lengua", Color(0xFF007AFF)),
            Asignatura("3", "Inglés", Color(0xFFFF9500)),
            Asignatura("4", "Ciencias", Color(0xFF34C759)),
            Asignatura("5", "Arte", Color(0xFFFF2D55)),
            Asignatura("6", "Música", Color(0xFF5AC8FA)),
            Asignatura("7", "Educación Física", Color(0xFFFF3B30))
        )
    }
    
    var trimestreSeleccionado by remember { mutableStateOf(Trimestre.PRIMERO) }
    var asignaturaSeleccionada by remember { mutableStateOf<Asignatura?>(null) }
    var alumnoSeleccionado by remember { mutableStateOf<Alumno?>(null) }
    var mostrarDialogoEvaluacion by remember { mutableStateOf(false) }
    
    // Filtro de búsqueda
    var searchQuery by remember { mutableStateOf("") }
    val alumnosFiltrados = remember(alumnos, searchQuery) {
        if (searchQuery.isEmpty()) {
            alumnos
        } else {
            alumnos.filter { alumno ->
                "${alumno.nombre} ${alumno.apellidos}".contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    // Datos simulados de evaluaciones
    val evaluaciones = remember {
        mutableStateListOf(
            Evaluacion(
                id = "1",
                alumnoId = "12345678A",
                trimestre = 1,
                asignatura = "Matemáticas",
                calificacion = 8.5f,
                comentarios = "Excelente comprensión de números y operaciones básicas.",
                fecha = Date()
            ),
            Evaluacion(
                id = "2",
                alumnoId = "12345678A",
                trimestre = 1,
                asignatura = "Lengua",
                calificacion = 7.0f,
                comentarios = "Buena lectura, necesita mejorar en escritura.",
                fecha = Date()
            ),
            Evaluacion(
                id = "3",
                alumnoId = "23456789B",
                trimestre = 1,
                asignatura = "Inglés",
                calificacion = 9.0f,
                comentarios = "Pronunciación excelente y buen vocabulario.",
                fecha = Date()
            ),
            Evaluacion(
                id = "4",
                alumnoId = "34567890C",
                trimestre = 1,
                asignatura = "Ciencias",
                calificacion = 6.5f,
                comentarios = "Necesita reforzar conceptos básicos.",
                fecha = Date()
            )
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evaluación Académica") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF5856D6),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    if (alumnoSeleccionado != null) {
                        mostrarDialogoEvaluacion = true
                    }
                },
                containerColor = Color(0xFF5856D6)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nueva evaluación",
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
            // Selector de trimestre
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
                    Text(
                        text = "Periodo de evaluación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Trimestre.values().forEach { trimestre ->
                            TrimestreChip(
                                trimestre = trimestre,
                                seleccionado = trimestre == trimestreSeleccionado,
                                onClick = { trimestreSeleccionado = trimestre }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Periodo: ${trimestreSeleccionado.fechaInicio} - ${trimestreSeleccionado.fechaFin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Selector de asignatura
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
                    Text(
                        text = "Asignatura",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.height(150.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            AsignaturaItem(
                                asignatura = null,
                                seleccionada = asignaturaSeleccionada == null,
                                onClick = { asignaturaSeleccionada = null }
                            )
                        }
                        items(asignaturas) { asignatura ->
                            AsignaturaItem(
                                asignatura = asignatura,
                                seleccionada = asignaturaSeleccionada == asignatura,
                                onClick = { asignaturaSeleccionada = asignatura }
                            )
                        }
                    }
                }
            }
            
            // Búsqueda de alumnos
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
            
            // Lista de alumnos
            Text(
                text = "Alumnos (${alumnosFiltrados.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (alumnosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron alumnos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(alumnosFiltrados) { alumno ->
                        AlumnoEvaluacionItem(
                            alumno = alumno,
                            seleccionado = alumno == alumnoSeleccionado,
                            evaluaciones = evaluaciones.filter { 
                                it.alumnoId == alumno.dni && 
                                it.trimestre == trimestreSeleccionado.ordinal + 1 &&
                                (asignaturaSeleccionada == null || it.asignatura == asignaturaSeleccionada?.nombre)
                            },
                            onClick = { alumnoSeleccionado = if (alumnoSeleccionado == alumno) null else alumno }
                        )
                    }
                }
            }
        }
        
        // Diálogo para añadir/editar evaluación
        if (mostrarDialogoEvaluacion) {
            AgregarEvaluacionDialog(
                alumno = alumnoSeleccionado!!,
                asignaturas = asignaturas,
                trimestre = trimestreSeleccionado,
                onDismiss = { mostrarDialogoEvaluacion = false },
                onGuardar = { asignatura, calificacion, comentarios ->
                    // Agregar nueva evaluación
                    evaluaciones.add(
                        Evaluacion(
                            id = UUID.randomUUID().toString(),
                            alumnoId = alumnoSeleccionado!!.dni,
                            trimestre = trimestreSeleccionado.ordinal + 1,
                            asignatura = asignatura.nombre,
                            calificacion = calificacion,
                            comentarios = comentarios,
                            fecha = Date()
                        )
                    )
                    mostrarDialogoEvaluacion = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrimestreChip(
    trimestre: Trimestre,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { 
            Text(
                text = trimestre.nombre,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            selectedContainerColor = Color(0xFF5856D6),
            selectedLabelColor = Color.White
        )
    )
}

@Composable
fun AsignaturaItem(
    asignatura: Asignatura?,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (seleccionada) 
                    (asignatura?.color ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.1f)
                else 
                    Color.Transparent
            )
            .border(
                width = if (seleccionada) 1.dp else 0.dp,
                color = if (seleccionada) 
                    (asignatura?.color ?: MaterialTheme.colorScheme.primary)
                else 
                    Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicador de color para la asignatura
        if (asignatura != null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(asignatura.color)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp).align(Alignment.Center)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = asignatura?.nombre ?: "Todas las asignaturas",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun AlumnoEvaluacionItem(
    alumno: Alumno,
    seleccionado: Boolean,
    evaluaciones: List<Evaluacion>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) 
                MaterialTheme.colorScheme.surfaceVariant
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    
                    Text(
                        text = "Clase: ${alumno.clase}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Indicador de cantidad de evaluaciones
                if (evaluaciones.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF5856D6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = evaluaciones.size.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Mostrar evaluaciones si está seleccionado
            if (seleccionado) {
                Spacer(modifier = Modifier.height(16.dp))
                
                if (evaluaciones.isEmpty()) {
                    Text(
                        text = "No hay evaluaciones registradas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        evaluaciones.forEach { evaluacion ->
                            EvaluacionItem(evaluacion = evaluacion)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvaluacionItem(
    evaluacion: Evaluacion
) {
    val calificacionColor = when {
        evaluacion.calificacion >= 9.0f -> Color(0xFF34C759) // Sobresaliente
        evaluacion.calificacion >= 7.0f -> Color(0xFF5AC8FA) // Notable
        evaluacion.calificacion >= 6.0f -> Color(0xFFFF9500) // Bien
        evaluacion.calificacion >= 5.0f -> Color(0xFFFFCC00) // Suficiente
        else -> Color(0xFFFF3B30) // Insuficiente
    }
    
    val calificacionTexto = when {
        evaluacion.calificacion >= 9.0f -> "Sobresaliente"
        evaluacion.calificacion >= 7.0f -> "Notable"
        evaluacion.calificacion >= 6.0f -> "Bien"
        evaluacion.calificacion >= 5.0f -> "Suficiente"
        else -> "Insuficiente"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = evaluacion.asignatura,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(calificacionColor)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = String.format("%.1f", evaluacion.calificacion),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = calificacionColor
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "($calificacionTexto)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (evaluacion.comentarios.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = evaluacion.comentarios,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Fecha de la evaluación
            Text(
                text = "Evaluado el ${
                    SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
                        .format(evaluacion.fecha)
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarEvaluacionDialog(
    alumno: Alumno,
    asignaturas: List<Asignatura>,
    trimestre: Trimestre,
    onDismiss: () -> Unit,
    onGuardar: (Asignatura, Float, String) -> Unit
) {
    var asignaturaSeleccionada by remember { mutableStateOf(asignaturas.first()) }
    var calificacion by remember { mutableFloatStateOf(5.0f) }
    var comentarios by remember { mutableStateOf("") }
    
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
                    text = "Nueva evaluación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Alumno: ${alumno.nombre} ${alumno.apellidos}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Text(
                    text = "Periodo: ${trimestre.nombre}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selector de asignatura
                Text(
                    text = "Asignatura",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Dropdown simplificado
                TextField(
                    value = asignaturaSeleccionada.nombre,
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = { /* Se implementaría un menú real aquí */ })
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selector de calificación
                Text(
                    text = "Calificación: ${String.format("%.1f", calificacion)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = calificacion,
                    onValueChange = { calificacion = it },
                    valueRange = 0f..10f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF5856D6),
                        activeTrackColor = Color(0xFF5856D6)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Comentarios
                OutlinedTextField(
                    value = comentarios,
                    onValueChange = { comentarios = it },
                    label = { Text("Comentarios (opcional)") },
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
                        onClick = { onGuardar(asignaturaSeleccionada, calificacion, comentarios) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5856D6)
                        )
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EvaluacionScreenPreview() {
    UmeEguneroTheme {
        EvaluacionScreen(
            navController = rememberNavController(),
            alumnos = listOf(
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
                )
            )
        )
    }
}
