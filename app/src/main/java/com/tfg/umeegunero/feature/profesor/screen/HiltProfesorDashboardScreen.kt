package com.tfg.umeegunero.feature.profesor.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.tfg.umeegunero.feature.profesor.viewmodel.ProfesorDashboardViewModel

/**
 * Componente que integra el ViewModel con la pantalla de dashboard del profesor
 *
 * Este componente sirve como puente entre el ViewModel y la UI, permitiendo
 * la inyección de dependencias a través de Hilt y proporcionando los datos
 * necesarios a la pantalla de dashboard del profesor.
 */
@Composable
fun HiltProfesorDashboardScreen(
    viewModel: ProfesorDashboardViewModel = hiltViewModel(),
    onLogout: () -> Unit,
    onNavigateToRegistroActividad: (String) -> Unit = {},
    onNavigateToDetalleAlumno: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Iniciar la carga de datos si es necesario
    if (!uiState.isLoading && (uiState.profesor == null || uiState.alumnos.isEmpty())) {
        viewModel.cargarDatosProfesor()
    }


    val mensajesFormateados = remember(uiState.mensajesNoLeidos) {
        uiState.mensajesNoLeidos.map { mensaje ->
            Triple(
                mensaje.emisorId,          // Primer elemento: ID del emisor
                mensaje.texto,             // Segundo elemento: texto del mensaje
                !mensaje.leido             // Tercer elemento: si no está leído
            )
        }
    }

    ProfesorDashboardScreen(
        onLogout = onLogout,
        onNavigateToRegistroActividad = onNavigateToRegistroActividad,
        onNavigateToDetalleAlumno = onNavigateToDetalleAlumno,
        onNavigateToChat = onNavigateToChat,
        alumnosPendientes = uiState.alumnosPendientes,
        alumnos = uiState.alumnos,
        mensajesNoLeidos = mensajesFormateados,
        totalMensajesNoLeidos = uiState.totalMensajesNoLeidos,
        isLoading = uiState.isLoading,
        error = uiState.error,
        selectedTab = uiState.selectedTab,
        onTabSelected = viewModel::setSelectedTab,
        onCrearRegistroActividad = viewModel::crearRegistroActividad,
        onErrorDismissed = viewModel::clearError
    )
}