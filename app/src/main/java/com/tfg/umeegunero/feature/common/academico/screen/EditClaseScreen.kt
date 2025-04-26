package com.tfg.umeegunero.feature.common.academico.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tfg.umeegunero.data.model.Clase
import com.tfg.umeegunero.feature.common.academico.viewmodel.GestionClasesViewModel
import com.tfg.umeegunero.feature.common.academico.viewmodel.AddClaseViewModel
import com.tfg.umeegunero.ui.components.LoadingIndicator
import androidx.compose.ui.text.font.FontWeight

/**
 * Pantalla para editar una clase existente
 * Reutiliza el componente AddClaseScreen pero en modo edición
 * 
 * @param navController Controlador de navegación
 * @param viewModel ViewModel para la gestión de clases
 */
@Composable
fun EditClaseScreen(
    navController: NavController,
    viewModel: AddClaseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Efecto para manejar la navegación después de un guardado exitoso
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar Clase", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.guardarClase() },
                icon = { Icon(Icons.Default.Check, contentDescription = "Guardar clase") },
                text = { Text("Guardar") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Nombre de la clase
                        OutlinedTextField(
                            value = uiState.nombre,
                            onValueChange = viewModel::updateNombre,
                            label = { Text("Nombre de la clase") },
                            placeholder = { Text("Ej: A, B, Mañana, etc.") },
                            isError = uiState.nombreError != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Aula
                        OutlinedTextField(
                            value = uiState.aula,
                            onValueChange = viewModel::updateAula,
                            label = { Text("Aula") },
                            placeholder = { Text("Ubicación física del aula") },
                            isError = uiState.aulaError != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Horario
                        OutlinedTextField(
                            value = uiState.horario,
                            onValueChange = viewModel::updateHorario,
                            label = { Text("Horario") },
                            placeholder = { Text("Ej: Lunes a Viernes 9:00-14:00") },
                            isError = uiState.horarioError != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Selector de profesor titular (opcional)
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Profesor Titular (Opcional)",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Se puede asignar posteriormente",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            if (uiState.isLoadingProfesores) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            } else if (uiState.profesoresDisponibles.isEmpty()) {
                                Text(
                                    text = "No hay profesores disponibles",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                ProfesorDropdown(
                                    profesores = uiState.profesoresDisponibles,
                                    selectedProfesorId = uiState.profesorTitularId,
                                    onProfesorSelected = viewModel::updateProfesorTitular,
                                    error = uiState.profesorTitularError,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "El profesor titular puede asignarse posteriormente en la pantalla de vinculación de profesores a clases",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp)
                                )
                            }
                        }
                        // Estado activo
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = uiState.activo,
                                    onCheckedChange = viewModel::updateActivo
                                )
                                Text(
                                    text = "Clase activa",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            Text(
                                text = "Una clase activa está operativa y visible para profesores y el centro educativo. Si se desactiva, la información se conserva pero no estará disponible para asignación de profesores.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 16.dp)
                            )
                        }
                        // Capacidad máxima con información adicional
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = uiState.capacidadMaxima,
                                onValueChange = { newValue ->
                                    if ((newValue.isEmpty() || newValue.all { it.isDigit() }) && newValue.length <= 3) {
                                        viewModel.updateCapacidadMaxima(newValue)
                                    }
                                },
                                label = { Text("Capacidad máxima") },
                                placeholder = { Text("Ej: 25") },
                                isError = uiState.capacidadMaximaError != null,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                supportingText = {
                                    if (uiState.capacidadMaximaError != null) {
                                        Text(
                                            text = when (uiState.capacidadMaximaError) {
                                                "La capacidad máxima es obligatoria" -> "⚠️ Este campo no puede estar vacío"
                                                "Introduce un número válido" -> "⚠️ Solo números enteros son permitidos"
                                                "La capacidad debe ser mayor que 0" -> "⚠️ El valor debe ser al menos 1"
                                                else -> "⚠️ ${uiState.capacidadMaximaError}"
                                            },
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    } else {
                                        Text(
                                            text = "Introduce un número positivo (máximo 999)",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Define el número máximo de alumnos que pueden asignarse a esta clase.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            // Indicador de carga
            if (uiState.isLoading) {
                LoadingIndicator(fullScreen = true)
            }
            // Mostrar errores generales mediante Snackbar
            LaunchedEffect(uiState.error) {
                uiState.error?.let { error ->
                    snackbarHostState.showSnackbar(message = error)
                    viewModel.clearError()
                }
            }
        }
    }
} 