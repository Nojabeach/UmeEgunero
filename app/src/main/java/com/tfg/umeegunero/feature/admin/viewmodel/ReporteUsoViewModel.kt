package com.tfg.umeegunero.feature.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.umeegunero.data.model.CaracteristicaUsada
import com.tfg.umeegunero.data.model.ReporteUsoUiState
import com.tfg.umeegunero.data.repository.EstadisticasRepository
import com.tfg.umeegunero.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel para la pantalla de reporte de uso.
 * 
 * Esta clase se encarga de gestionar el estado de la UI y realizar operaciones 
 * relacionadas con los reportes de uso de la aplicación. Interactúa con el repositorio
 * de estadísticas para obtener datos reales desde Firestore.
 * 
 * @property estadisticasRepository Repositorio para acceder a los datos de estadísticas en Firestore
 */
@HiltViewModel
class ReporteUsoViewModel @Inject constructor(
    private val estadisticasRepository: EstadisticasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReporteUsoUiState())
    val uiState: StateFlow<ReporteUsoUiState> = _uiState.asStateFlow()

    init {
        cargarDatosUso()
    }

    /**
     * Carga los datos de uso desde el repositorio de estadísticas.
     * Si no existen datos, genera datos ficticios para demostración.
     */
    fun cargarDatosUso() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Cargar estadísticas generales
                val periodo = _uiState.value.periodoSeleccionado
                when (val resultado = estadisticasRepository.obtenerEstadisticasUso(periodo)) {
                    is Result.Success -> {
                        val estadisticas = resultado.data
                        val usuariosActivos = (estadisticas["usuariosActivos"] as? Long)?.toInt() ?: 0
                        val sesionesPromedio = estadisticas["sesionesPromedio"] as? Double ?: 0.0
                        val tiempoPromedioSesion = estadisticas["tiempoPromedioSesion"] as? String ?: "0 min"
                        val ultimaActualizacion = estadisticas["ultimaActualizacion"] as? Timestamp
                        val fechaActualizacionStr = ultimaActualizacion?.let {
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            sdf.format(Date(it.seconds * 1000))
                        } ?: "No disponible"
                        
                        // Cargar características más usadas
                        when (val resultadoCaracteristicas = estadisticasRepository.obtenerCaracteristicasUsadas()) {
                            is Result.Success -> {
                                val caracteristicas = resultadoCaracteristicas.data
                                
                                _uiState.update { it.copy(
                                    isLoading = false,
                                    caracteristicasUsadas = caracteristicas,
                                    usuariosActivos = usuariosActivos,
                                    sesionesPromedio = sesionesPromedio,
                                    tiempoPromedioSesion = tiempoPromedioSesion,
                                    ultimaActualizacion = ultimaActualizacion,
                                    fechaActualizacion = fechaActualizacionStr
                                ) }
                            }
                            is Result.Error -> {
                                Timber.e(resultadoCaracteristicas.exception, "Error al cargar características")
                                _uiState.update { it.copy(
                                    isLoading = false,
                                    error = resultadoCaracteristicas.exception?.message ?: "Error al cargar características"
                                ) }
                            }
                            is Result.Loading -> {
                                // No hacer nada mientras carga, ya estamos mostrando un indicador de carga
                            }
                        }
                    }
                    is Result.Error -> {
                        Timber.e(resultado.exception, "Error al cargar estadísticas")
                        
                        // Si no hay datos, intentamos generar datos ficticios
                        generarDatosFicticios()
                    }
                    is Result.Loading -> {
                        // No hacer nada mientras carga, ya estamos mostrando un indicador de carga
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error inesperado al cargar datos de uso")
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error inesperado al cargar datos de uso"
                ) }
            }
        }
    }
    
    /**
     * Genera datos ficticios para demostración en caso de que no existan
     * datos reales en Firestore.
     */
    private suspend fun generarDatosFicticios() {
        try {
            when (val resultado = estadisticasRepository.generarDatosFicticios()) {
                is Result.Success -> {
                    Timber.d("Datos ficticios generados correctamente")
                    // Cargar los datos recién generados
                    cargarDatosUso()
                }
                is Result.Error -> {
                    Timber.e(resultado.exception, "Error al generar datos ficticios")
                    
                    // En caso de error, mostramos datos ficticios locales como último recurso
                    val caracteristicas = listOf(
                        CaracteristicaUsada("Gestión de centros", 245, 28.5f),
                        CaracteristicaUsada("Comunicados", 186, 21.7f),
                        CaracteristicaUsada("Calendario", 157, 18.3f),
                        CaracteristicaUsada("Gestión de profesores", 132, 15.4f),
                        CaracteristicaUsada("Gestión de clases", 89, 10.4f),
                        CaracteristicaUsada("Reportes", 49, 5.7f)
                    )

                    val timestamp = Timestamp.now()
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val fechaActualizacionStr = sdf.format(Date(timestamp.seconds * 1000))

                    _uiState.update { it.copy(
                        isLoading = false,
                        caracteristicasUsadas = caracteristicas,
                        usuariosActivos = 127,
                        sesionesPromedio = 4.3,
                        tiempoPromedioSesion = "12 min",
                        ultimaActualizacion = timestamp,
                        fechaActualizacion = fechaActualizacionStr
                    ) }
                }
                is Result.Loading -> {
                    // No hacer nada mientras carga, ya estamos mostrando un indicador de carga
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error al generar datos ficticios")
            _uiState.update { it.copy(
                isLoading = false,
                error = "Error al generar datos: ${e.message}"
            ) }
        }
    }

    /**
     * Limpia los mensajes de error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Actualiza el periodo seleccionado para el reporte y recarga los datos
     * @param periodo Nuevo periodo seleccionado (ej. "Último mes", "Última semana")
     */
    fun updatePeriodoSeleccionado(periodo: String) {
        _uiState.update { it.copy(periodoSeleccionado = periodo) }
        cargarDatosUso() // Recargar datos con el nuevo periodo
    }

    /**
     * Registra el uso de la característica "Reportes" y genera un reporte PDF simulado
     */
    fun generarReporte() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isGeneratingReport = true) }

                // Registramos el uso de la característica "Reportes"
                estadisticasRepository.registrarUsoCaracteristica("Reportes")
                
                // Simulamos el proceso de generación de PDF
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