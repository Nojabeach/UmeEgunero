package com.tfg.umeegunero.feature.centro.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.feature.centro.viewmodel.AddAlumnoViewModel
import java.time.LocalDate as JavaLocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.draw.alpha

/**
 * Pantalla para añadir un nuevo alumno al sistema
 * Permite al administrador de centro registrar alumnos con todos sus datos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlumnoScreen(
    navController: NavController,
    viewModel: AddAlumnoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Mostrar mensajes de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    // Manejar navegación en caso de éxito
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            // Navegar de vuelta a la lista de alumnos
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Alumno") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            val isEnabled = uiState.isFormValid && !uiState.isLoading
            FloatingActionButton(
                onClick = { if (isEnabled) viewModel.guardarAlumno() },
                modifier = Modifier.alpha(if (isEnabled) 1f else 0.6f)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Guardar")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loader
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            // Sección de datos personales
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Datos Personales",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // DNI
                    OutlinedTextField(
                        value = uiState.dni,
                        onValueChange = { viewModel.updateDni(it) },
                        label = { Text("DNI/NIE") },
                        isError = uiState.dniError != null,
                        supportingText = { uiState.dniError?.let { Text(it) } },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    // Nombre
                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = { viewModel.updateNombre(it) },
                        label = { Text("Nombre") },
                        isError = uiState.nombreError != null,
                        supportingText = { uiState.nombreError?.let { Text(it) } },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    // Apellidos
                    OutlinedTextField(
                        value = uiState.apellidos,
                        onValueChange = { viewModel.updateApellidos(it) },
                        label = { Text("Apellidos") },
                        isError = uiState.apellidosError != null,
                        supportingText = { uiState.apellidosError?.let { Text(it) } },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    // Fecha de nacimiento
                    OutlinedTextField(
                        value = uiState.fechaNacimiento,
                        onValueChange = { viewModel.updateFechaNacimiento(it) },
                        label = { Text("Fecha de nacimiento") },
                        isError = uiState.fechaNacimientoError != null,
                        supportingText = { uiState.fechaNacimientoError?.let { Text(it) } },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, "Seleccionar fecha")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        readOnly = true // Para que solo se pueda cambiar con el DatePicker
                    )
                }
            }
            
            // Sección de datos académicos
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Datos Académicos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Selector de curso
                    ExposedDropdownMenuBox(
                        expanded = uiState.isCursoDropdownExpanded,
                        onExpandedChange = { viewModel.toggleCursoDropdown() }
                    ) {
                        OutlinedTextField(
                            value = uiState.cursoSeleccionado?.nombre ?: "Seleccione un curso",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.isCursoDropdownExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Curso") },
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = uiState.isCursoDropdownExpanded,
                            onDismissRequest = { viewModel.toggleCursoDropdown() }
                        ) {
                            uiState.cursos.forEach { curso ->
                                DropdownMenuItem(
                                    text = { Text("${curso.nombre} (${curso.anioAcademico})") },
                                    onClick = {
                                        viewModel.selectCurso(curso)
                                        viewModel.toggleCursoDropdown()
                                    }
                                )
                            }
                        }
                    }
                    
                    // Selector de clase (solo disponible si hay curso seleccionado)
                    if (uiState.cursoSeleccionado != null) {
                        ExposedDropdownMenuBox(
                            expanded = uiState.isClaseDropdownExpanded,
                            onExpandedChange = { viewModel.toggleClaseDropdown() }
                        ) {
                            OutlinedTextField(
                                value = uiState.claseSeleccionada?.nombre ?: "Seleccione una clase",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.isClaseDropdownExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                label = { Text("Clase") },
                                leadingIcon = { Icon(Icons.Default.Class, contentDescription = null) }
                            )
                            
                            ExposedDropdownMenu(
                                expanded = uiState.isClaseDropdownExpanded,
                                onDismissRequest = { viewModel.toggleClaseDropdown() }
                            ) {
                                uiState.clases.forEach { clase ->
                                    DropdownMenuItem(
                                        text = { Text(clase.nombre) },
                                        onClick = {
                                            viewModel.selectClase(clase)
                                            viewModel.toggleClaseDropdown()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Sección de datos médicos
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Información Médica",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Alergias
                    OutlinedTextField(
                        value = uiState.alergias,
                        onValueChange = { viewModel.updateAlergias(it) },
                        label = { Text("Alergias (separadas por comas)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null) },
                        minLines = 2
                    )
                    
                    // Medicación
                    OutlinedTextField(
                        value = uiState.medicacion,
                        onValueChange = { viewModel.updateMedicacion(it) },
                        label = { Text("Medicación (separada por comas)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null) },
                        minLines = 2
                    )
                    
                    // Necesidades especiales
                    OutlinedTextField(
                        value = uiState.necesidadesEspeciales,
                        onValueChange = { viewModel.updateNecesidadesEspeciales(it) },
                        label = { Text("Necesidades especiales") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Accessibility, contentDescription = null) },
                        minLines = 3
                    )
                }
            }
            
            // Sección de observaciones
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Observaciones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = uiState.observaciones,
                        onValueChange = { viewModel.updateObservaciones(it) },
                        label = { Text("Observaciones generales") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        minLines = 5
                    )
                }
            }
            
            // Espacio para el FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
    
    // DatePicker para la fecha de nacimiento
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                val date = if (uiState.fechaNacimiento.isNotBlank()) {
                    JavaLocalDate.parse(uiState.fechaNacimiento, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } else {
                    JavaLocalDate.now().minusYears(6) // Edad escolar típica
                }
                date.toEpochDay() * 24 * 60 * 60 * 1000
            } catch (e: Exception) {
                JavaLocalDate.now().minusYears(6).toEpochDay() * 24 * 60 * 60 * 1000
            }
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = JavaLocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        val formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        viewModel.updateFechaNacimiento(formattedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
} 