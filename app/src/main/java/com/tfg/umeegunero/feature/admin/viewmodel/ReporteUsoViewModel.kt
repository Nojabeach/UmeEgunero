package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.CaracteristicaUsada
import com.tfg.umeegunero.data.model.ReporteUsoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel para la pantalla de reporte de uso
 */
@HiltViewModel
class ReporteUsoViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReporteUsoUiState())
    val uiState: StateFlow<ReporteUsoUiState> = _uiState.asStateFlow()

    init {
        cargarDatosUso()
    }

    /**
     * Carga los datos de uso desde Firestore
     */
    fun cargarDatosUso() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Aquí iría la lógica para cargar datos reales desde Firestore
                // Por ahora, usaremos datos ficticios
                delay(1000) // Simular carga de red

                val caracteristicas = listOf(
                    CaracteristicaUsada("Gestión de centros", 245, 28.5f),
                    CaracteristicaUsada("Comunicados", 186, 21.7f),
                    CaracteristicaUsada("Calendario", 157, 18.3f),
                    CaracteristicaUsada("Gestión de profesores", 132, 15.4f),
                    CaracteristicaUsada("Gestión de clases", 89, 10.4f),
                    CaracteristicaUsada("Reportes", 49, 5.7f)
                )

                _uiState.update { it.copy(
                    isLoading = false,
                    caracteristicasUsadas = caracteristicas,
                    usuariosActivos = 127,
                    sesionesPromedio = 4.3,
                    tiempoPromedioSesion = "12 min"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar datos de uso"
                ) }
            }
        }
    }

    /**
     * Limpia los mensajes de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Actualiza el periodo seleccionado para el reporte
     */
    fun updatePeriodoSeleccionado(periodo: String) {
        _uiState.update { it.copy(periodoSeleccionado = periodo) }
        cargarDatosUso() // Recargar datos con el nuevo periodo
    }

    /**
     * Genera un reporte PDF con los datos de uso
     */
    fun generarReporte() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isGeneratingReport = true) }

                // Aquí iría la lógica para generar el PDF o exportar datos
                // Simulamos el proceso
                delay(2000)

                _uiState.update { it.copy(
                    isGeneratingReport = false,
                    reportGenerated = true
                ) }

                // Resetear estado después de un tiempo
                delay(3000)
                _uiState.update { it.copy(reportGenerated = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isGeneratingReport = false,
                    error = e.message ?: "Error al generar el reporte"
                ) }
            }
        }
    }
} 