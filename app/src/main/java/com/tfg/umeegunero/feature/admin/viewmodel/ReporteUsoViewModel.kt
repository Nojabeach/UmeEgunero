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
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * Estado de la UI para la pantalla de reportes de uso
 */
data class ReporteUsoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Estadísticas generales
    val totalUsuarios: Int = 0,
    val totalAccesos: Int = 0,
    val promedioUsoDiario: Int = 0,
    
    // Porcentajes
    val tasaCrecimiento: Float = 0f,
    val tasaEngagement: Float = 0f,
    val tasaRetencion: Float = 0f,
    
    // Distribución por tipo de usuario
    val totalAdministradores: Int = 0,
    val totalProfesores: Int = 0,
    val totalAlumnos: Int = 0,
    val totalFamiliares: Int = 0,
    
    // Datos para gráficos
    val usuariosActivosPorMes: List<Int> = List(12) { 0 },
    val accesosPorHora: List<Int> = List(24) { 0 },
    val usoPorCentro: Map<String, Int> = emptyMap()
)

/**
 * ViewModel para la gestión de reportes de uso de la plataforma
 */
@HiltViewModel
class ReporteUsoViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReporteUsoUiState(isLoading = true))
    val uiState: StateFlow<ReporteUsoUiState> = _uiState.asStateFlow()

    /**
     * Carga los datos de uso de la plataforma
     */
    fun cargarDatosUso() {
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
                Timber.e(e, "Error al cargar datos de uso")
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
            // Datos generales
            val totalUsuarios = 1583
            val totalAccesos = 36428
            val promedioUsoDiario = 27
            
            // Tasas
            val tasaCrecimiento = 8.7f
            val tasaEngagement = 12.5f
            val tasaRetencion = 85.2f
            
            // Distribución por tipo
            val totalAdministradores = 18
            val totalProfesores = 112
            val totalAlumnos = 987
            val totalFamiliares = 466
            
            // Datos para gráficos
            val usuariosActivosPorMes = listOf(142, 156, 168, 173, 185, 203, 198, 173, 201, 245, 267, 290)
            val accesosPorHora = listOf(12, 8, 5, 3, 2, 8, 35, 120, 230, 180, 150, 142, 220, 230, 190, 170, 160, 130, 110, 85, 62, 48, 32, 20)
            
            // Uso por centro
            val usoPorCentro = mapOf(
                "CEIP San José" to 420,
                "IES María Moliner" to 350,
                "Colegio Santa Ana" to 310,
                "CEIP Juan Ramón Jiménez" to 280,
                "IES Miguel de Cervantes" to 223
            )
            
            // Actualizamos el estado
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    totalUsuarios = totalUsuarios,
                    totalAccesos = totalAccesos,
                    promedioUsoDiario = promedioUsoDiario,
                    tasaCrecimiento = tasaCrecimiento,
                    tasaEngagement = tasaEngagement,
                    tasaRetencion = tasaRetencion,
                    totalAdministradores = totalAdministradores,
                    totalProfesores = totalProfesores,
                    totalAlumnos = totalAlumnos,
                    totalFamiliares = totalFamiliares,
                    usuariosActivosPorMes = usuariosActivosPorMes,
                    accesosPorHora = accesosPorHora,
                    usoPorCentro = usoPorCentro
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