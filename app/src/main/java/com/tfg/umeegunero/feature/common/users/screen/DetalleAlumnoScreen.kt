package com.tfg.umeegunero.feature.common.users.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.common.users.viewmodel.DetalleAlumnoViewModel
import com.tfg.umeegunero.data.model.Alumno
import com.tfg.umeegunero.util.Result
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleAlumnoScreen(
    dni: String,
    onNavigateBack: () -> Unit,
    viewModel: DetalleAlumnoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Log al iniciar la composición
    Timber.d("DetalleAlumnoScreen: Iniciando con DNI: '$dni'")

    LaunchedEffect(dni) {
        Timber.d("DetalleAlumnoScreen: LaunchedEffect con DNI: '$dni'")
        viewModel.cargarDetallesAlumno(dni)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Alumno") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val result = uiState.alumno) {
                is Result.Loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando información del alumno...")
                    }
                }
                is Result.Success -> {
                    val alumno = result.data
                    Timber.d("DetalleAlumnoScreen: Mostrando alumno: ${alumno.nombre} ${alumno.apellidos}")
                    DetallesAlumnoContent(
                        alumno = alumno,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is Result.Error -> {
                    val errorMsg = result.exception?.message ?: "Error desconocido"
                    Timber.e("DetalleAlumnoScreen: Error mostrando detalles de alumno: $errorMsg")
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No se pudo cargar la información del alumno",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.cargarDetallesAlumno(dni) }
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun DetallesAlumnoContent(
    alumno: Alumno,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Información del Alumno",
            style = MaterialTheme.typography.headlineSmall
        )

        // Detalles básicos
        Text(
            text = "DNI: ${alumno.dni}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Nombre: ${alumno.nombre} ${alumno.apellidos}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Curso: ${alumno.curso}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Clase: ${alumno.clase}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Estado: ${if (alumno.activo) "Activo" else "Inactivo"}",
            style = MaterialTheme.typography.bodyLarge,
            color = if (alumno.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        
        // Detalles adicionales si están disponibles
        if (alumno.fechaNacimiento.isNotBlank()) {
            Text(
                text = "Fecha de nacimiento: ${alumno.fechaNacimiento}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        if (alumno.email.isNotBlank()) {
            Text(
                text = "Email: ${alumno.email}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        if (alumno.telefono.isNotBlank()) {
            Text(
                text = "Teléfono: ${alumno.telefono}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        if (alumno.necesidadesEspeciales.isNotBlank()) {
            Text(
                text = "Necesidades especiales: ${alumno.necesidadesEspeciales}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        if (alumno.alergias.isNotEmpty()) {
            Text(
                text = "Alergias: ${alumno.alergias.joinToString(", ")}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
} 