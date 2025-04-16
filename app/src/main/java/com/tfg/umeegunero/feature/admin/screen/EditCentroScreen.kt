package com.tfg.umeegunero.feature.admin.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tfg.umeegunero.feature.admin.viewmodel.AddCentroViewModel
import kotlin.math.max
import com.tfg.umeegunero.feature.admin.screen.AddCentroScreenContent
import com.tfg.umeegunero.feature.admin.screen.DeleteCentroConfirmationDialog

/**
 * Pantalla para editar un centro educativo existente.
 * Esta pantalla reutiliza los componentes de AddCentroScreen pero precarga los datos del centro a editar.
 * 
 * @param navController Controlador de navegación
 * @param centroId ID del centro a editar
 * @param viewModel ViewModel para la edición de centros
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCentroScreen(
    navController: NavController,
    centroId: String,
    viewModel: AddCentroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val provinciasLista by viewModel.provincias.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Cargar los datos del centro al iniciar
    LaunchedEffect(centroId) {
        if (!centroId.isBlank() && uiState.id.isBlank()) {
            viewModel.loadCentro(centroId)
        }
    }
    
    // Observar cambios en el estado y navegar de vuelta si la operación fue exitosa
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navController.popBackStack()
        }
    }
    
    // Calcular el porcentaje de completado del formulario
    val porcentajeCompletado = calcularPorcentajeCompletado(uiState)
    
    // Efecto para mostrar errores en el Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }
    
    // Estado para el diálogo de confirmación de eliminación
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Editar Centro Educativo",
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    // Botón de eliminar
                    IconButton(
                        onClick = {
                            showDeleteConfirmation = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar centro",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    // Botón de guardar
                    IconButton(
                        onClick = { 
                            viewModel.guardarCentro()
                        },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar cambios"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Reutilizamos el contenido principal de AddCentroScreen
        AddCentroScreenContent(
            uiState = uiState,
            provincias = provinciasLista,
            porcentajeCompletado = porcentajeCompletado,
            isEditMode = true,
            onNombreChange = viewModel::updateNombre,
            onCalleChange = viewModel::updateCalle,
            onNumeroChange = viewModel::updateNumero,
            onCodigoPostalChange = viewModel::updateCodigoPostal,
            onCiudadChange = viewModel::updateCiudad,
            onProvinciaChange = viewModel::updateProvincia,
            onTelefonoChange = viewModel::updateTelefono,
            onCiudadSelected = viewModel::seleccionarCiudad,
            onToggleMapa = viewModel::toggleMapa,
            onSaveClick = {
                viewModel.guardarCentro()
            },
            onCancelClick = { navController.popBackStack() },
            onAddAdminCentro = viewModel::addAdminCentro,
            onRemoveAdminCentro = viewModel::removeAdminCentro,
            onUpdateAdminCentroDni = viewModel::updateAdminCentroDni,
            onUpdateAdminCentroNombre = viewModel::updateAdminCentroNombre,
            onUpdateAdminCentroApellidos = viewModel::updateAdminCentroApellidos,
            onUpdateAdminCentroEmail = viewModel::updateAdminCentroEmail,
            onUpdateAdminCentroTelefono = viewModel::updateAdminCentroTelefono,
            onUpdateAdminCentroPassword = viewModel::updateAdminCentroPassword,
            modifier = Modifier.padding(paddingValues)
        )
        
        // Diálogo de confirmación para eliminar centro
        if (showDeleteConfirmation) {
            DeleteCentroConfirmationDialog(
                onConfirm = {
                    viewModel.deleteCentro(uiState.id)
                    showDeleteConfirmation = false
                },
                onDismiss = {
                    showDeleteConfirmation = false
                }
            )
        }
    }
}

/**
 * Calcula el porcentaje de llenado del formulario
 */
private fun calcularPorcentajeCompletado(uiState: AddCentroViewModel.AddCentroState): Float {
    var camposRequeridos = 7 // Nombre, calle, número, CP, ciudad, provincia, admin principal
    var camposLlenos = 0
    
    // Campos básicos del centro
    if (uiState.nombre.isNotBlank()) camposLlenos++
    if (uiState.calle.isNotBlank()) camposLlenos++
    if (uiState.numero.isNotBlank()) camposLlenos++
    if (uiState.codigoPostal.isNotBlank()) camposLlenos++
    if (uiState.ciudad.isNotBlank()) camposLlenos++
    if (uiState.provincia.isNotBlank()) camposLlenos++
    
    // Verificar el administrador principal
    val adminPrincipal = uiState.adminCentro.firstOrNull()
    if (adminPrincipal != null) {
        var camposAdmin = 5
        var camposAdminLlenos = 0
        
        if (adminPrincipal.dni.isNotBlank()) camposAdminLlenos++
        if (adminPrincipal.nombre.isNotBlank()) camposAdminLlenos++
        if (adminPrincipal.apellidos.isNotBlank()) camposAdminLlenos++
        if (adminPrincipal.email.isNotBlank()) camposAdminLlenos++
        
        // La contraseña solo es obligatoria para centros nuevos
        if (uiState.isEdit && adminPrincipal.password.isBlank()) {
            camposAdmin--
        } else if (adminPrincipal.password.isNotBlank()) {
            camposAdminLlenos++
        }
        
        // Sumar fracción de los campos del admin principal completos
        camposLlenos += (camposAdminLlenos.toFloat() / camposAdmin * 1).toInt()
    }
    
    // Peso adicional para verificar si hay errores
    val tieneErrores = uiState.nombreError != null ||
            uiState.calleError != null ||
            uiState.numeroError != null ||
            uiState.codigoPostalError != null ||
            uiState.ciudadError != null ||
            uiState.provinciaError != null ||
            uiState.telefonoError != null ||
            uiState.adminCentroError != null
    
    // Si hay errores, reducir el porcentaje
    val porcentaje = if (tieneErrores) {
        max(0.1f, camposLlenos.toFloat() / camposRequeridos - 0.2f)
    } else {
        camposLlenos.toFloat() / camposRequeridos
    }
    
    return porcentaje.coerceIn(0f, 1f)
}

/**
 * Cuenta el número de errores en todos los campos del formulario
 */
private fun countErrors(uiState: AddCentroViewModel.AddCentroState): Int {
    var errorCount = 0
    
    // Errores en los campos principales
    if (uiState.nombreError != null) errorCount++
    if (uiState.calleError != null) errorCount++
    if (uiState.numeroError != null) errorCount++
    if (uiState.codigoPostalError != null) errorCount++
    if (uiState.ciudadError != null) errorCount++
    if (uiState.provinciaError != null) errorCount++
    if (uiState.telefonoError != null) errorCount++
    
    // Errores en administradores
    uiState.adminCentro.forEach { admin ->
        if (admin.dniError != null) errorCount++
        if (admin.nombreError != null) errorCount++
        if (admin.apellidosError != null) errorCount++
        if (admin.emailError != null) errorCount++
        if (admin.telefonoError != null) errorCount++
        if (admin.passwordError != null) errorCount++
    }
    
    // Error global de administradores
    if (uiState.adminCentroError != null) errorCount++
    
    return errorCount
} 