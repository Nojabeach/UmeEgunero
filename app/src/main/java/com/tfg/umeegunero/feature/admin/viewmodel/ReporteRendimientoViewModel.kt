package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Modelo para representar un error del sistema
 */
data class ErrorSistema(
    val mensaje: String,
    val nivel: String,
    val fecha: String,
    val componente: String
)

/**
 * Estado de la UI para la pantalla de reportes de rendimiento
 */
data class ReporteRendimientoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Estado de los componentes del sistema
    val estadoServidores: String = "Normal",
    val estadoBaseDatos: String = "Normal",
    val estadoAlmacenamiento: String = "Normal",
    
    // Tiempos de respuesta
    val tiemposRespuesta: List<Int> = emptyList(),
    val periodosRespuesta: List<String> = emptyList(),
    val tiempoRespuestaPromedio: Int = 0,
    
    // Uso de recursos
    val usoCPU: Int = 0,
    val usoMemoria: Int = 0,
    val usoAlmacenamiento: Int = 0,
    val usoAnchoBanda: Int = 0,
    
    // Errores del sistema
    val erroresSistema: List<ErrorSistema> = emptyList()
)

/**
 * ViewModel para la gestión de reportes de rendimiento del sistema
 */
@HiltViewModel
class ReporteRendimientoViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReporteRendimientoUiState(isLoading = true))
    val uiState: StateFlow<ReporteRendimientoUiState> = _uiState.asStateFlow()

    /**
     * Carga los datos de rendimiento del sistema
     */
    fun cargarDatosRendimiento() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // En un caso real, estos datos se obtendrían de Firestore o una API
                // Para esta demo, usamos datos de prueba
                cargarDatosPrueba()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "Error al cargar datos: ${e.message}",
                        isLoading = false
                    ) 
                }
                Timber.e(e, "Error al cargar datos de rendimiento")
            }
        }
    }

    /**
     * Carga datos de prueba para la demo
     * En una implementación real, estos datos vendrían de Firestore
     */
    private fun cargarDatosPrueba() {
        // Simulamos una carga de datos
        viewModelScope.launch {
            // Estado de los componentes
            val estadoServidores = "Normal"
            val estadoBaseDatos = "Normal"
            val estadoAlmacenamiento = "Normal"
            
            // Tiempos de respuesta (últimas 24 horas, cada hora)
            val tiemposRespuesta = listOf(
                220, 210, 230, 190, 205, 210, 240, 260, 280, 290, 275, 260,
                250, 230, 210, 200, 190, 185, 195, 220, 230, 240, 235, 225
            )
            val periodosRespuesta = listOf(
                "00:00", "01:00", "02:00", "03:00", "04:00", "05:00", 
                "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
                "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", 
                "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
            )
            val tiempoRespuestaPromedio = tiemposRespuesta.average().toInt()
            
            // Uso de recursos
            val usoCPU = 45
            val usoMemoria = 62
            val usoAlmacenamiento = 53
            val usoAnchoBanda = 38
            
            // Errores del sistema (muestra una lista vacía para estado normal)
            val erroresSistema = listOf<ErrorSistema>(
                // Si hubiera errores, se verían así:
                // ErrorSistema(
                //     mensaje = "Tiempo de respuesta elevado en la API de usuarios",
                //     nivel = "Medio",
                //     fecha = "15/05/2023 14:32:45",
                //     componente = "API Gateway"
                // )
            )
            
            // Actualizamos el estado
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    estadoServidores = estadoServidores,
                    estadoBaseDatos = estadoBaseDatos,
                    estadoAlmacenamiento = estadoAlmacenamiento,
                    tiemposRespuesta = tiemposRespuesta,
                    periodosRespuesta = periodosRespuesta,
                    tiempoRespuestaPromedio = tiempoRespuestaPromedio,
                    usoCPU = usoCPU,
                    usoMemoria = usoMemoria,
                    usoAlmacenamiento = usoAlmacenamiento,
                    usoAnchoBanda = usoAnchoBanda,
                    erroresSistema = erroresSistema
                ) 
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 