package com.tfg.umeegunero.feature.profesor.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.feature.profesor.models.Rubrica
import com.tfg.umeegunero.feature.profesor.viewmodel.EvaluacionUiState
import com.tfg.umeegunero.feature.profesor.viewmodel.EvaluacionViewModel

/**
 * Pantalla principal para gestionar evaluaciones académicas.
 * Permite seleccionar alumnos, rúbricas y crear/editar evaluaciones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluacionScreen(
    viewModel: EvaluacionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evaluación académica") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is EvaluacionUiState.Success) {
                val successState = uiState as EvaluacionUiState.Success
                // Solo mostrar el FAB si hay un alumno y una rúbrica seleccionados
                if (successState.alumnoSeleccionado != null && successState.rubricaSeleccionada != null) {
                    FloatingActionButton(onClick = { viewModel.mostrarDialogoEvaluacion() }) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva evaluación")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (uiState) {
                is EvaluacionUiState.Loading -> {
                    // Mostrar indicador de carga
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Cargando datos...")
                    }
                }
                is EvaluacionUiState.Success -> {
                    val state = uiState as EvaluacionUiState.Success
                    
                    // Filtros superiores
                    FiltrosEvaluacion(
                        asignaturas = state.asignaturas,
                        asignaturaSeleccionada = state.asignaturaSeleccionada,
                        onAsignaturaSelected = { viewModel.seleccionarAsignatura(it) },
                        trimestre = state.trimestreSeleccionado,
                        onTrimestreSelected = { viewModel.seleccionarTrimestre(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Selector de alumno
                    AlumnoSelector(
                        alumnos = state.alumnos,
                        alumnoSeleccionado = state.alumnoSeleccionado,
                        onAlumnoSelected = { viewModel.seleccionarAlumno(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Selector de rúbrica
                    RubricaSelector(
                        rubricas = state.rubricas,
                        rubricaSeleccionada = state.rubricaSeleccionada,
                        onRubricaSelected = { viewModel.seleccionarRubrica(it) },
                        onCrearRubrica = { viewModel.mostrarDialogoRubrica() }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Lista de evaluaciones
                    if (state.alumnoSeleccionado != null) {
                        ListaEvaluaciones(
                            evaluaciones = state.evaluaciones,
                            rubricaSeleccionada = state.rubricaSeleccionada,
                            onEliminarEvaluacion = { /* TODO */ }
                        )
                    } else {
                        // Mensaje para seleccionar un alumno
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text("Selecciona un alumno para ver sus evaluaciones")
                        }
                    }
                    
                    // TODO: Diálogos para crear/editar rúbricas y evaluaciones
                }
                is EvaluacionUiState.Error -> {
                    // Mostrar mensaje de error
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (uiState as EvaluacionUiState.Error).mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente para los filtros de evaluación (asignatura y trimestre)
 */
@Composable
fun FiltrosEvaluacion(
    asignaturas: List<String>,
    asignaturaSeleccionada: String,
    onAsignaturaSelected: (String) -> Unit,
    trimestre: Int,
    onTrimestreSelected: (Int) -> Unit
) {
    Column {
        // Selector de asignatura
        Text(
            text = "Asignatura",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        var expandedAsignatura by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedAsignatura,
            onExpandedChange = { expandedAsignatura = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                readOnly = true,
                value = asignaturaSeleccionada.ifEmpty { "Selecciona asignatura" },
                onValueChange = { },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAsignatura) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedAsignatura,
                onDismissRequest = { expandedAsignatura = false }
            ) {
                asignaturas.forEach { asignatura ->
                    DropdownMenuItem(
                        text = { Text(asignatura) },
                        onClick = {
                            onAsignaturaSelected(asignatura)
                            expandedAsignatura = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Selector de trimestre
        Text(
            text = "Trimestre",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TabRow(selectedTabIndex = trimestre - 1) {
            Tab(
                selected = trimestre == 1,
                onClick = { onTrimestreSelected(1) },
                text = { Text("1er Trimestre") }
            )
            Tab(
                selected = trimestre == 2,
                onClick = { onTrimestreSelected(2) },
                text = { Text("2do Trimestre") }
            )
            Tab(
                selected = trimestre == 3,
                onClick = { onTrimestreSelected(3) },
                text = { Text("3er Trimestre") }
            )
        }
    }
}

/**
 * Selector de alumno
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnoSelector(
    alumnos: List<Alumno>,
    alumnoSeleccionado: Alumno?,
    onAlumnoSelected: (Alumno?) -> Unit
) {
    Column {
        Text(
            text = "Alumno",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                readOnly = true,
                value = alumnoSeleccionado?.let { "${it.nombre} ${it.apellidos}" } ?: "Selecciona alumno",
                onValueChange = { },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                alumnos.forEach { alumno ->
                    DropdownMenuItem(
                        text = { Text("${alumno.nombre} ${alumno.apellidos}") },
                        onClick = {
                            onAlumnoSelected(alumno)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Selector de rúbrica
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RubricaSelector(
    rubricas: List<Rubrica>,
    rubricaSeleccionada: Rubrica?,
    onRubricaSelected: (Rubrica?) -> Unit,
    onCrearRubrica: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Rúbrica",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCrearRubrica) {
                Icon(Icons.Default.Add, contentDescription = "Crear rúbrica")
            }
        }
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                readOnly = true,
                value = rubricaSeleccionada?.nombre ?: "Selecciona rúbrica",
                onValueChange = { },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                rubricas.forEach { rubrica ->
                    DropdownMenuItem(
                        text = { Text(rubrica.nombre) },
                        onClick = {
                            onRubricaSelected(rubrica)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * Lista de evaluaciones para el alumno seleccionado
 */
@Composable
fun ListaEvaluaciones(
    evaluaciones: List<com.tfg.umeegunero.feature.profesor.models.EvaluacionRubrica>,
    rubricaSeleccionada: Rubrica?,
    onEliminarEvaluacion: (String) -> Unit
) {
    Column {
        Text(
            text = "Evaluaciones",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (evaluaciones.isEmpty()) {
            // Mensaje para cuando no hay evaluaciones
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No hay evaluaciones registradas")
            }
        } else {
            // Lista de evaluaciones
            LazyColumn {
                items(evaluaciones) { evaluacion ->
                    EvaluacionItem(
                        evaluacion = evaluacion,
                        onEliminar = { onEliminarEvaluacion(evaluacion.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Item individual de evaluación
 */
@Composable
fun EvaluacionItem(
    evaluacion: com.tfg.umeegunero.feature.profesor.models.EvaluacionRubrica,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Fecha y calificación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fecha: ${evaluacion.fecha}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Calificación: ${evaluacion.calificacionFinal}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Comentarios generales
            if (evaluacion.comentariosGenerales.isNotEmpty()) {
                Text(
                    text = "Comentarios:",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = evaluacion.comentariosGenerales,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // TODO: Se podría añadir un botón para ver detalles de la evaluación
        }
    }
} 